package com.devappsys.grpc_logs.api.data_models;
import androidx.annotation.NonNull;

import com.devappsys.log.Log;
import com.google.protobuf.Timestamp;

import java.util.Map;

public class LogModel {

    private String logID;
    private int level; // LogLevel enum value

    private String contextID;
    private String message;
    private String stackTrace;
    private Timestamp loggedAt; // This will be a timestamp from Protobuf
    Map<String,Object> logProperties ;

    // Default constructor
    public LogModel() {}

    // Constructor with all fields
    public LogModel( int level, String contextID, String message, String stackTrace, Timestamp loggedAt, Map<String,Object> logProperties) {
        this.logID = java.util.UUID.randomUUID().toString(); // Generate a unique log ID
        this.level = level;
        this.contextID = contextID;
        this.message = message;
        this.stackTrace = stackTrace;
        this.loggedAt = loggedAt;
        this.logProperties=logProperties;
    }

    /**
     * Converts a LogModel to a LogMessage Protobuf object.
     * @return LogMessage Protobuf object
     */
    public Log.LogMessage toProtobuf() {
        // Create a LogMessage builder to construct the Protobuf object
        Log.LogMessage.Builder logMessageBuilder = Log.LogMessage.newBuilder();

        // Map fields from LogModel to LogMessage
        logMessageBuilder.setUuid(this.logID)
                .setLevel(Log.LogLevel.forNumber(this.level)) // Map int to LogLevel enum
                .setContextId(this.contextID) // Map int to LogType enum
                .setMessage(this.message)
                .setStackTrace(this.stackTrace)
                .setLoggedAt(this.loggedAt)
                .putAllLogProperties(this.getLogPropertiesToMap());


        // Return the built LogMessage object
        return logMessageBuilder.build();
    }

    private Map<String, String> getLogPropertiesToMap() {
        if (logProperties == null || logProperties.isEmpty()) {
            return new java.util.HashMap<>();
        }
        Map<String, String> logPropertiesMap = new java.util.HashMap<>();
        for (Map.Entry<String, Object> entry : logProperties.entrySet()) {
            logPropertiesMap.put(entry.getKey(), entry.getValue().toString());
        }
        return logPropertiesMap;
    }

    // Getters and setters for each field

    public String getLogID() {
        return logID;
    }

    public void setLogID(String logID) {
        this.logID = logID;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }


    public Timestamp getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(Timestamp loggedAt) {
        this.loggedAt = loggedAt;
    }




    // Optional: Override toString for easier logging/printing
    @NonNull
    @Override
    public String toString() {
        return "LogModel{" +
                "logID='" + logID + '\'' +
                ", level=" + level +
                ", contextID=" + contextID +
                ", message='" + message + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", loggedAt=" + loggedAt +
                '}';
    }
}