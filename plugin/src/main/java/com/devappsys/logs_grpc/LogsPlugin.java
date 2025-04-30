package com.devappsys.logs_grpc;


import static com.devappsys.logs_grpc.util.MemoryUtil.getFreeMemory;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import com.devappsys.log.Log;
import com.devappsys.logs_grpc.grpc.GrpcClient;
import com.devappsys.logs_grpc.grpc.GrpcClientBlockingImpl;
import com.devappsys.logs_grpc.listener.NetworkChangeReceiver;
import com.devappsys.logs_grpc.local_datasouce.FileDatasourceImpl;
import com.devappsys.logs_grpc.local_datasouce.LocalDatasourceRepo;
import com.devappsys.logs_grpc.models.Configuration;
import com.devappsys.logs_grpc.models.data.ContextModel;
import com.devappsys.logs_grpc.models.data.EventModel;
import com.devappsys.logs_grpc.models.data.LogModel;
import com.devappsys.logs_grpc.worker.UploadWorker;
import com.google.protobuf.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
        networkChangeReceiver = new NetworkChangeReceiver((carrierName, carrierId) -> {
            logContext(
                    configuration.getDeviceId(),
                    configuration.getAppVersion(),
                    configuration.getPackageName(),
                    configuration.getUserId(),
                    sessionId,
                    "en",
                    carrierId,
                    carrierName,
                    "",
                    false
            );
        });

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
            // worker trigger
            logAppCrash(throwable);
            _instance.localDatasourceRepo.rotateEmergency();
            Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler()).uncaughtException(thread, throwable);
        });
    }

    // core methods
    public static void logAppCrash(Throwable throwable) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }

        // Generate event details
        String eventID = java.util.UUID.randomUUID().toString();  // Unique event ID
        String eventName = "App Crash";                            // Event name
        String screenName = "Unknown";                             // Screen name, can be set to "Unknown" as crash occurs unexpectedly

        // Prepare stack trace
        String stackTrace = Arrays.toString(throwable.getStackTrace());

        // Prepare custom attributes with crash info
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("error_message", throwable.getMessage());
        customAttributes.put("stack_trace", stackTrace);

        // Log the crash event using logEvent
        logEvent(eventID, eventName, screenName,  customAttributes);
    }

    private static void logInternal(Log.LogLevel level, Log.LogType type, String tag, String message, Throwable throwable) {
        if (_instance == null) throw new IllegalStateException("LogsPlugin is not initialized");

        String stackTrace = (throwable != null) ? Arrays.toString(throwable.getStackTrace()) : "";

        _instance.localDatasourceRepo.saveLog(
                new LogModel(
                        level.ordinal(),
                        type.ordinal(),
                        tag + ": " + message,
                        stackTrace,
                        _instance.sessionId,
                        Timestamp.getDefaultInstance(),
                        getFreeMemory(_instance.context),
                        _instance._configuration.getPackageName(),
                        _instance._configuration.getClientId(),
                        _instance._configuration.getPackageName()
                )
        );

        android.util.Log.d(TAG, "Log: " + message + " | Level: " + level + " | Type: " + type);
    }

    public static void logEvent(String eventID, String eventName, String screenName,
                                boolean appOpened, boolean appBackgrounded, boolean sessionStarted,
                                boolean sessionEnded, double latitude, double longitude, String city,
                                String region, String country, String carrier, boolean dynamicConfigChanged,
                                Map<String, Object> customAttributes) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        Timestamp eventTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();

        EventModel eventModel = new EventModel(
                eventID,
                eventName,
                screenName,
                eventTime,
                appOpened,
                appBackgrounded,
                sessionStarted,
                sessionEnded,
                latitude,
                longitude,
                city,
                region,
                country,
                carrier,
                dynamicConfigChanged,
                customAttributes
        );

        _instance.localDatasourceRepo.saveEvent(eventModel);
    }

    public static void logEvent(String eventID, String eventName, String screenName,  Map<String, Object> customAttributes) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        Timestamp eventTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();

        // Create the EventModel object
        EventModel eventModel = new EventModel(
                eventID,
                eventName,
                screenName,
                eventTime,
                false,  // App is not opened or backgrounded here; this is just an activity event.
                false,  // Same here; app state might not be relevant
                false,  // Session start and end are not part of this event.
                false,
                0.0,    // Latitude, longitude can be added if needed
                0.0,
                "",     // City, region, country info can be added if needed
                "",
                "",
                "",     // Carrier can be added if needed
                false,   // Dynamic config changes can be added if needed
                customAttributes
        );

        // Save the event
        _instance.localDatasourceRepo.saveEvent(eventModel);
    }


    // helper methods
    public static void logDebug(String tag, String message) {
        logInternal(Log.LogLevel.DEBUG, Log.LogType.NON_FATAL, tag, message, null);
    }

    public static void logInfo(String tag, String message) {
        logInternal(Log.LogLevel.INFO, Log.LogType.NON_FATAL, tag, message, null);
    }

    public static void logWarning(String tag, String message) {
        logInternal(Log.LogLevel.WARN, Log.LogType.UNRECOGNIZED, tag, message, null);
    }

    public static void logError(String tag, String message, Throwable throwable) {
        logInternal(Log.LogLevel.ERROR, Log.LogType.FATAL_CRASH, tag, message, throwable);
    }

// log    methods end


// Specific Event Logging Methods (App Opened, Session Started, etc.)

    public static void logAppOpened(String eventID, String screenName,
                                    double latitude, double longitude, String city, String region,
                                    String country, String carrier, Map<String, Object> customAttributes) {
        logEvent(eventID, "App Opened", screenName, true, false, false, false, latitude,
                longitude, city, region, country, carrier, false, customAttributes);
    }

    public static void logAppBackgrounded(String eventID, String screenName,
                                          double latitude, double longitude, String city, String region,
                                          String country, String carrier, Map<String, Object> customAttributes) {
        logEvent(eventID, "App Backgrounded", screenName,  false, true, false, false, latitude,
                longitude, city, region, country, carrier, false, customAttributes);
    }

    public static void logSessionStarted(String eventID, String screenName,
                                         double latitude, double longitude, String city, String region,
                                         String country, String carrier, Map<String, Object> customAttributes) {
        logEvent(eventID, "Session Started", screenName,false, false, true, false, latitude,
                longitude, city, region, country, carrier, false, customAttributes);
    }

    public static void logSessionEnded(String eventID, String screenName,
                                       double latitude, double longitude, String city, String region,
                                       String country, String carrier, Map<String, Object> customAttributes) {
        logEvent(eventID, "Session Ended", screenName, false, false, false, true, latitude,
                longitude, city, region, country, carrier, false, customAttributes);
    }

    public static void logActivityLifecycleEvent(String activityName, String eventType) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }

        // Create event information
        String eventID = java.util.UUID.randomUUID().toString(); // Unique event ID
        String eventName = "Activity Lifecycle - " + eventType;   // Event type can be "Created", "Started", "Resumed", "Paused", "Stopped", "Destroyed"
        String screenName = activityName;  // Activity name
        Timestamp eventTime = Timestamp.newBuilder().setSeconds(System.currentTimeMillis() / 1000).build();  // Current time

        // Additional details (you can add any other information that’s relevant, such as location, app state, etc.)
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put("activity_name", activityName);
        customAttributes.put("event_type", eventType);

        // Log the event using logEvent
        logEvent(eventID, eventName, screenName, customAttributes);
    }

    public static void logContext(String deviceID, String appVersion, String appPackageName, String userID,
                                  String sessionID, String language, String networkStatus, String location,
                                  String ipAddress, boolean isRooted) {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }

        // Create the ContextModel object
        ContextModel contextModel = new ContextModel(
                deviceID,
                appVersion,
                appPackageName,
                userID,
                sessionID,
                language,
                networkStatus,
                location,
                ipAddress,
                isRooted
        );

        // Save the context using the localDatasourceRepo
        _instance.localDatasourceRepo.saveContext(contextModel);

        // Optionally, log or handle additional logic like validating the context or triggering an event
        // Log that the context was saved successfully (for debugging or monitoring purposes)
        android.util.Log.d(TAG, "Context data saved: " + contextModel);
    }


    // Unregister the network change receiver when no longer needed (e.g., in Activity or Service onDestroy)
    public void destroy() {
        if (networkChangeReceiver != null) {
            context.unregisterReceiver(networkChangeReceiver);
        }
    }

    // ActivityLifecycleCallbacks to track activity lifecycle events
    private static class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Created");
        }

        @Override
        public void onActivityStarted(Activity activity) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Started");
        }

        @Override
        public void onActivityResumed(Activity activity) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Resumed");
        }

        @Override
        public void onActivityPaused(Activity activity) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Paused");
        }

        @Override
        public void onActivityStopped(Activity activity) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Stopped");
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, @NonNull Bundle outState) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "SaveInstanceState");
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            logActivityLifecycleEvent(activity.getLocalClassName(), "Destroyed");
        }
    }
}