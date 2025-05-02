package com.devappsys.grpc_logs.api;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.devappsys.grpc_logs.api.data_models.ContextModel;
import com.devappsys.grpc_logs.api.data_models.EventModel;
import com.devappsys.grpc_logs.api.data_models.LogModel;
import com.devappsys.grpc_logs.api.grpc.GrpcClientAsyncImpl;
import com.devappsys.grpc_logs.api.listeners.NetworkChangeReceiver;
import com.devappsys.grpc_logs.api.local_datasouce.FileDatasourceImpl;
import com.devappsys.grpc_logs.api.local_datasouce.LocalDatasourceRepo;
import com.devappsys.grpc_logs.util.EmulatorUtil;
import com.devappsys.grpc_logs.util.IPUtil;
import com.devappsys.grpc_logs.util.Logger;
import com.devappsys.log.Common;
import com.devappsys.log.Event;
import com.devappsys.log.Log;
import com.google.protobuf.Timestamp;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.grpc.stub.StreamObserver;

public class GrpcPlugin {
    private static final String TAG = GrpcPlugin.class.getName();
    private static GrpcPlugin instance;
    protected Context context;
    private final String sessionId;
    private final Configuration configuration;
    private final String sdkVersion = "1.0.0";
    AtomicInteger logCounter = new AtomicInteger(0);
    AtomicInteger eventCounter = new AtomicInteger(0);
    /**
     * Whether or not the SDK is in the process of uploading events.
     */
    AtomicBoolean logsUploading = new AtomicBoolean(false);
    AtomicBoolean eventsUploading = new AtomicBoolean(false);
    AtomicBoolean contextUploading = new AtomicBoolean(false);
    /**
     * This thread is used for offline tasks like db insertion, file writing etc.
     */
    WorkerThread logThread = new WorkerThread("logThread");
    /**
     * This thread is used for network tasks like sending logs to server.
     */
    WorkerThread networkThread = new WorkerThread("networkThread");
    /**
     * This context will hold current context information
     */
    private ContextModel contextModel;
    private boolean isNetworkAvailable = false;
    private final GrpcClientAsyncImpl grpcClientAsync;
    private DeviceInfo deviceInfo;
    private LocalDatasourceRepo localDatasourceRepo;
    private Logger logger;

    private GrpcPlugin(Context context, String sessionId, Configuration configuration) {
        this.context = context.getApplicationContext();
        this.sessionId = sessionId;
        this.configuration = configuration;
        this.deviceInfo = new DeviceInfo(context);
        logThread.start();
        networkThread.start();
        refreshContextInternal();
        NetworkChangeReceiver networkChangeReceiver = new NetworkChangeReceiver((carrierName, carrierId, networkAvailable) -> {
            this.isNetworkAvailable = networkAvailable;
            refreshContextInternal();
        });
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkChangeReceiver, filter);
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logInternal(Log.LogLevel.CRASH_VALUE, throwable.getMessage(), android.util.Log.getStackTraceString(throwable), null);
            localDatasourceRepo.rotateEmergency();

            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
        localDatasourceRepo = FileDatasourceImpl.getInstance(context);
        grpcClientAsync = new GrpcClientAsyncImpl(configuration.getHost(), configuration.getPort());
        if (context instanceof Application) {
            Application application = (Application) context;
            application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        } else {
            logger.w( "Context is not an instance of Application. Activity lifecycle callbacks will not be registered.");
        }
    }

    public static class Builder {
        private Context context;
        private String userId;
        private String deviceId;
        private String host;
        private int port;
        private long batchSize;
        private int appId;

        private boolean isDebug = true;

        public synchronized Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public synchronized Builder setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public synchronized Builder setDebug(boolean debug) {
            this.isDebug = debug;
            return this;
        }

        public synchronized Builder setAppId(int appId) {
            this.appId = appId;
            return this;
        }

        public synchronized Builder setDeviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public synchronized Builder setHost(String host) {
            this.host = host;
            return this;
        }

        public synchronized Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public synchronized Builder setBatchSize(long batchSize) {
            this.batchSize = batchSize;
            return this;
        }

        public synchronized GrpcPlugin build() {
            if (context == null) {
                throw new IllegalArgumentException("Context cannot be null");
            }
            if (host == null) {
                throw new IllegalArgumentException("Host cannot be null");
            }
            if (port <= 0) {
                throw new IllegalArgumentException("Port cannot be less than or equal to 0");
            }
            if (deviceId == null) {
                throw new IllegalArgumentException("Device ID cannot be null");
            }
            if (userId == null) {
                throw new IllegalArgumentException("User ID cannot be null");
            }
            if (batchSize <= 0) {
                throw new IllegalArgumentException("Batch size must be greater than 0");
            }
            if (instance != null) {
                throw new IllegalStateException("GrpcPlugin is already initialized");
            }
            if (appId <= 0) {
                throw new IllegalArgumentException("App ID must be greater than 0");
            }
            String sessionId = UUID.randomUUID().toString();
            if (instance == null) {
                instance = new GrpcPlugin(context, sessionId, new Configuration(host, port, deviceId, appId, userId, batchSize));
            }
            instance.logger = new Logger(isDebug);
            return instance;
        }
    }

    protected void logInternal(int level, String message, String stackTrace, Map<String, Object> logProperties) {
        Timestamp loggedTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
        runOnLogThread(() -> {
            LogModel logModel = new LogModel(level, contextModel.getContextID(), message, stackTrace, loggedTime, logProperties);
            localDatasourceRepo.saveLog(logModel);

            if (logCounter.incrementAndGet() >= configuration.getBatchSize()) {
                try {
                    localDatasourceRepo.rotateLogsFile();
                    logCounter.set(0);
                } catch (IOException ignored) {
                }
                if (!isNetworkAvailable) {
                    return;
                }
                if (logsUploading.compareAndSet(false, true)) {
                    networkThread.post(() -> {
                        try {
                            Map<String, Log.LogBatch> logBatches = localDatasourceRepo.getAllLogs();
                            for (Map.Entry<String, Log.LogBatch> entry : logBatches.entrySet()) {
                                String fileName = entry.getKey(); // Prevent closure issue
                                grpcClientAsync.uploadLogs(entry.getValue(), new StreamObserver<>() {
                                    @Override
                                    public void onNext(Common.UploadResponse value) {
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        t.printStackTrace(); // Optional retry logic here
                                    }

                                    @Override
                                    public void onCompleted() {
                                        localDatasourceRepo.deleteLogsFile(fileName);
                                    }
                                });
                            }
                        } finally {
                            logsUploading.set(false);
                        }
                    });
                }
            }
        });
    }

    protected void logEventInternal(String eventType, String displayName, String message, Map<String, Object> customAttributes) {
        Timestamp eventTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();
        logger.d("logEventInternal: " + eventType + " " + displayName + " " + message);
        runOnLogThread(() -> {
            // Save the event locally
            EventModel eventModel = new EventModel(
                    contextModel.getContextID(),
                    eventType,
                    eventTime,
                    displayName,
                    message,
                    customAttributes
            );
            localDatasourceRepo.saveEvent(eventModel);

            // Check if we need to rotate and upload
            if (eventCounter.incrementAndGet() >= configuration.getBatchSize()) {
                try {
                    localDatasourceRepo.rotateEventsFile();
                    eventCounter.set(0);
                } catch (IOException ignored) {
                }
                if (!isNetworkAvailable) {
                    return;
                }

                // Prevent concurrent uploads
                if (eventsUploading.compareAndSet(false, true)) {
                    networkThread.post(() -> {
                        try {
                            Map<String, Event.EventBatch> eventBatches = localDatasourceRepo.getAllEvents();

                            for (Map.Entry<String, Event.EventBatch> entry : eventBatches.entrySet()) {
                                String fileName = entry.getKey(); // Avoid closure issues
                                grpcClientAsync.uploadEvents(entry.getValue(), new StreamObserver<>() {
                                    @Override
                                    public void onNext(Common.UploadResponse value) {
                                    }

                                    @Override
                                    public void onError(Throwable t) {
                                        t.printStackTrace(); // Optional: Add retry/backoff
                                    }

                                    @Override
                                    public void onCompleted() {
                                        localDatasourceRepo.deleteEventsFile(fileName);
                                    }
                                });
                            }

                        } finally {
                            eventsUploading.set(false);
                        }
                    });
                }
            }
        });
    }

    protected void logContextInternal() {
        runOnLogThread(() -> {
            contextModel = new ContextModel(sessionId, configuration.getUserId(), configuration.getDeviceId(), EmulatorUtil.isEmulator() ? "Emulator" : "device", deviceInfo.getManufacturer(), deviceInfo.getCarrier(), deviceInfo.getBrand(), deviceInfo.getOsName(), deviceInfo.getOsVersion(), configuration.getAppId(), deviceInfo.getVersionName(), deviceInfo.getOsVersion(), sdkVersion, "en", IPUtil.getIPAddress(true), "", "", "", 0, 0);
            localDatasourceRepo.saveContext(contextModel);
            if (!isNetworkAvailable) {
                return;
            }
            if (contextUploading.compareAndSet(false, true)) {
                networkThread.post(() -> {
                    try {
                        grpcClientAsync.uploadContext(contextModel.toProtobuf(), new StreamObserver<>() {
                            @Override
                            public void onNext(Common.UploadResponse value) {
                            }

                            @Override
                            public void onError(Throwable t) {
                                t.printStackTrace();
                            }

                            @Override
                            public void onCompleted() {
                            }
                        });
                    } finally {
                        contextUploading.set(false);
                    }
                });
            }
        });
    }

    public static void logActivityLifecycleEventInternal(String activityName, String eventType) {
        if (instance == null) {
            return;
        }

        instance.logEventInternal(eventType, activityName, eventType + " " + activityName, null);
    }

    private static class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        private long lastLoggedTime = 0;
        private static final long DEBOUNCE_INTERVAL_MS = 2000;

        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Created");
            }
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Started");
            }
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Resumed");
            }
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Paused");
            }
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Stopped");
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "SaveInstanceState");
            }
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
            if (System.currentTimeMillis() - lastLoggedTime > DEBOUNCE_INTERVAL_MS) {
                lastLoggedTime = System.currentTimeMillis();
                logActivityLifecycleEventInternal(activity.getLocalClassName(), "Destroyed");
            }
        }
    }

    void refreshContextInternal() {
        logContextInternal();
    }

    protected void runOnLogThread(Runnable r) {
        if (Thread.currentThread() != logThread) {
            logThread.post(r);
        } else {
            r.run();
        }
    }
}
