package com.devappsys.logs_grpc;

import static com.devappsys.logs_grpc.util.MemoryUtil.getFreeMemory;
import static com.devappsys.logs_grpc.util.StorageUtil.getFreeStorage;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.devappsys.logs_grpc.grpc.GrpcClient;
import com.devappsys.logs_grpc.grpc.GrpcClientBlockingImpl;
import com.devappsys.logs_grpc.listener.NetworkChangeReceiver;
import com.devappsys.logs_grpc.local_datasouce.FileDatasourceImpl;
import com.devappsys.logs_grpc.local_datasouce.LocalDatasourceRepo;
import com.devappsys.logs_grpc.models.Configuration;
import com.devappsys.logs_grpc.models.data.LogModel;
import com.google.protobuf.Timestamp;

import java.util.Objects;

public class LogsPlugin {
    private static LogsPlugin _instance;
    public static final String TAG = "LogsPlugin";

    private Configuration _configuration;
    private GrpcClient _client;
    private String sessionId;
    private NetworkChangeReceiver networkChangeReceiver;
    private Context context;

    private LocalDatasourceRepo localDatasourceRepo;

    private LogsPlugin(Context context, Configuration configuration) {
        this.context = context;
        this._configuration = configuration;
        GrpcClientBlockingImpl.init(configuration.getHost(), configuration.getPort());
        this._client = GrpcClientBlockingImpl.getInstance();
        this.sessionId = java.util.UUID.randomUUID().toString();


        localDatasourceRepo = new FileDatasourceImpl(context);

        // Register the network change receiver
        networkChangeReceiver = new NetworkChangeReceiver(networkType -> {
            // Handle network change callback (e.g., log or process the new network state)
            System.out.println("Network changed to: " + networkType);
        });

        // Register for connectivity changes
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(networkChangeReceiver, filter);
        // Register the ActivityLifecycleCallbacks to listen to activity changes
        if (context instanceof Application) {
            Application application = (Application) context;
            application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks());
        } else {
            throw new IllegalStateException("Context must be of type Application");
        }
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
                        getFreeStorage(),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                )
        );
        _instance._client.sendLog(
                new LogModel(
                        1, 1, message, message,
                        _instance.sessionId, Timestamp.getDefaultInstance().getDefaultInstanceForType(),
                        getFreeMemory(_instance.context),
                        getFreeStorage(),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                ).toProtobuf()
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
            log("Activity Created: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStarted(Activity activity) {
            // Handle activity started
            System.out.println("Activity Started: " + activity.getLocalClassName());
            log("Activity Started: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityResumed(Activity activity) {
            // Handle activity resumed
            System.out.println("Activity Resumed: " + activity.getLocalClassName());
            log("Activity Resumed: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityPaused(Activity activity) {
            // Handle activity paused
            System.out.println("Activity Paused: " + activity.getLocalClassName());
            log("Activity Paused: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityStopped(Activity activity) {
            // Handle activity stopped
            System.out.println("Activity Stopped: " + activity.getLocalClassName());
            log("Activity Stopped: " + activity.getLocalClassName());
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            // Handle activity saving state
            System.out.println("Activity SaveInstanceState: " + activity.getLocalClassName());
            log("Activity SaveInstanceState: " + activity.getLocalClassName());
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            // Handle activity destroyed
            System.out.println("Activity Destroyed: " + activity.getLocalClassName());
            log("Activity Destroyed: " + activity.getLocalClassName());
        }
    }
}