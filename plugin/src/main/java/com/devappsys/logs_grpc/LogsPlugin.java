package com.devappsys.logs_grpc;


import android.content.SharedPreferences;
import static com.devappsys.logs_grpc.util.MemoryUtil.getFreeMemory;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import com.devappsys.logs_grpc.worker.UploadWorker;

import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.devappsys.logs_grpc.grpc.GrpcClient;
import com.devappsys.logs_grpc.grpc.GrpcClientBlockingImpl;
import com.devappsys.logs_grpc.listener.NetworkChangeReceiver;
import com.devappsys.logs_grpc.local_datasouce.FileDatasourceImpl;
import com.devappsys.logs_grpc.local_datasouce.LocalDatasourceRepo;
import com.devappsys.logs_grpc.models.Configuration;
import com.devappsys.logs_grpc.models.data.LogModel;
import com.google.protobuf.Timestamp;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class LogsPlugin {
    private static LogsPlugin _instance;
    public static final String TAG = "LogsPlugin";
    public static final String PREF_NAME = "logs_plugin_prefs";
    public static final String KEY_SESSION_ID = "session_id";
    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_PACKAGE_NAME = "package_name";
    public static final String KEY_CLIENT_ID = "client_id";
    private final Configuration _configuration;
    private final GrpcClient _client;
    private final String sessionId;
    private final NetworkChangeReceiver networkChangeReceiver;
    private final Context context;

    private final LocalDatasourceRepo localDatasourceRepo;

    public GrpcClient getGrpcClient() {
        return _client;
    }

    public Configuration getConfiguration() {
        return _configuration;
    }

    public LocalDatasourceRepo getLocalDatasourceRepo() {
        return localDatasourceRepo;
    }

    public String getSessionId() {
        return sessionId;
    }

    private LogsPlugin(Context context, Configuration configuration) {
        this.context = context;
        this._configuration = configuration;
        GrpcClientBlockingImpl.init(configuration.getHost(), configuration.getPort());
        this._client = GrpcClientBlockingImpl.getInstance();
        this.sessionId = java.util.UUID.randomUUID().toString();


        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SESSION_ID, sessionId);
        editor.putString(KEY_HOST, configuration.getHost());
        editor.putInt(KEY_PORT, configuration.getPort());
        editor.putString(KEY_PACKAGE_NAME, configuration.getPackageName());
        editor.putString(KEY_CLIENT_ID, configuration.getClientId());
        editor.apply();

        localDatasourceRepo = FileDatasourceImpl.getInstance(context);

        // Register the network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(LogsPlugin::logContext);

        // Register for connectivity changes
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkChangeReceiver, filter);



        if (context instanceof Application) {
            Application application = (Application) context;
            application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        } else {
            throw new IllegalStateException("Context must be of type Application");
        }
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build();
        PeriodicWorkRequest uploadRequest = new PeriodicWorkRequest.Builder(
                UploadWorker.class,
                15, TimeUnit.MINUTES
        ).setConstraints(constraints).build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "log_upload_worker",                             // Unique name
                ExistingPeriodicWorkPolicy.UPDATE,                 // Policy
                uploadRequest                                    // The request
        );
    }

    public static boolean isInitialized() {
        return _instance != null;
    }

    public static LogsPlugin getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        return _instance;
    }

    // Initialization method
    public static void init(Context context, Configuration configuration) {
        if (_instance != null) {
            throw new IllegalStateException("LogsPlugin is already initialized");
        }
        _instance = new LogsPlugin(context, configuration);

        // ✅ Register the custom uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            System.out.println("🚨 Uncaught Exception in thread: " + thread.getName());
// log
            // worker trigger
            logEvent("🚨 Uncaught Exception in thread: " + thread.getName() + " - " + throwable.getMessage() + " - " + Arrays.toString(throwable.getStackTrace()));
            _instance.localDatasourceRepo.rotateEmergency();
            Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(thread, throwable);
        });
    }


    public static void log(String message) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        _instance.localDatasourceRepo.saveLog(
                new LogModel(
                        1, 1, message, message,
                        _instance.sessionId, Timestamp.getDefaultInstance().getDefaultInstanceForType(),
                        getFreeMemory(_instance.context),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                )
        );
    }

    public static void logEvent(String event){
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        _instance.localDatasourceRepo.saveLog(
                new LogModel(
                        1, 1, event, event,
                        _instance.sessionId, Timestamp.getDefaultInstance().getDefaultInstanceForType(),
                        getFreeMemory(_instance.context),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                )
        );
    }


    public static void logContext(String context){
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        _instance.localDatasourceRepo.saveLog(
                new LogModel(
                        1, 1, context, context,
                        _instance.sessionId, Timestamp.getDefaultInstance().getDefaultInstanceForType(),
                        getFreeMemory(_instance.context),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                )
        );
    }


    // Unregister the network change receiver when no longer needed (e.g., in Activity or Service onDestroy)
    public void unregisterNetworkReceiver() {
        if (networkChangeReceiver != null) {
            context.unregisterReceiver(networkChangeReceiver);
        }
    }

    // ActivityLifecycleCallbacks to track activity lifecycle events
    private static class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            // Handle activity creation
            System.out.println("Activity Created: " + activity.getLocalClassName());
            logEvent("Activity Created: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStarted(Activity activity) {
            // Handle activity started
            System.out.println("Activity Started: " + activity.getLocalClassName());
            logEvent("Activity Started: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // Handle activity resumed
            System.out.println("Activity Resumed: " + activity.getLocalClassName());
            logEvent("Activity Resumed: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // Handle activity paused
            System.out.println("Activity Paused: " + activity.getLocalClassName());
            logEvent("Activity Paused: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            // Handle activity stopped
            System.out.println("Activity Stopped: " + activity.getLocalClassName());
            logEvent("Activity Stopped: " + activity.getLocalClassName());
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // Handle activity saving state
            System.out.println("Activity SaveInstanceState: " + activity.getLocalClassName());
            logEvent("Activity SaveInstanceState: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            // Handle activity destroyed
            System.out.println("Activity Destroyed: " + activity.getLocalClassName());
            logEvent("Activity Destroyed: " + activity.getLocalClassName());
        }
    }
}