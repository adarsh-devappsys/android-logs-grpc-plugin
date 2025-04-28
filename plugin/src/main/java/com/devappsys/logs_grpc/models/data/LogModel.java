package com.devappsys.logs_grpc.models.data;

import com.devappsys.log.Log;
import com.google.protobuf.Timestamp;

public class LogModel {

    private String logID;
    private int level; // LogLevel enum value
    private int type; // LogType enum value
    private String message;
    private String stackTrace;
    private String sessionID;
    private Timestamp loggedAt; // This will be a timestamp from Protobuf
    private double freeRAMMB;
    private double freeStorageMB;
    private String deviceModel;
    private String deviceOS;
    private String deviceOSVersion;

    // Default constructor
    public LogModel() {}

    // Constructor with all fields
    public LogModel( int level, int type, String message, String stackTrace,
                    String sessionID, Timestamp loggedAt, double freeRAMMB,
                    double freeStorageMB, String deviceModel, String deviceOS, String deviceOSVersion) {
        this.logID = java.util.UUID.randomUUID().toString(); // Generate a unique log ID
        this.level = level;
        this.type = type;
        this.message = message;
        this.stackTrace = stackTrace;
        this.sessionID = sessionID;
        this.loggedAt = loggedAt;
        this.freeRAMMB = freeRAMMB;
        this.freeStorageMB = freeStorageMB;
        this.deviceModel = deviceModel;
        this.deviceOS = deviceOS;
        this.deviceOSVersion = deviceOSVersion;
    }

    /**
     * Converts a LogModel to a LogMessage Protobuf object.
     * @return LogMessage Protobuf object
     */
    public Log.LogMessage toProtobuf() {
        // Create a LogMessage builder to construct the Protobuf object
        Log.LogMessage.Builder logMessageBuilder = Log.LogMessage.newBuilder();

        // Map fields from LogModel to LogMessage
        logMessageBuilder.setLogID(this.logID)
                .setLevel(Log.LogLevel.forNumber(this.level)) // Map int to LogLevel enum
                .setType(Log.LogType.forNumber(this.type)) // Map int to LogType enum
                .setMessage(this.message)
                .setStackTrace(this.stackTrace)
                .setSessionID(this.sessionID)
                .setLoggedAt(this.loggedAt)
                .setFreeRAMMB(this.freeRAMMB)
                .setFreeStorageMB(this.freeStorageMB)
                .setDeviceModel(this.deviceModel)
                .setDeviceOS(this.deviceOS)
                .setDeviceOSVersion(this.deviceOSVersion);

        // Return the built LogMessage object
        return logMessageBuilder.build();
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public Timestamp getLoggedAt() {
        return loggedAt;
    }

    public void setLoggedAt(Timestamp loggedAt) {
        this.loggedAt = loggedAt;
    }

    public double getFreeRAMMB() {
        return freeRAMMB;
    }

    public void setFreeRAMMB(double freeRAMMB) {
        this.freeRAMMB = freeRAMMB;
    }

    public double getFreeStorageMB() {
        return freeStorageMB;
    }

    public void setFreeStorageMB(double freeStorageMB) {
        this.freeStorageMB = freeStorageMB;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getDeviceOS() {
        return deviceOS;
    }

    public void setDeviceOS(String deviceOS) {
        this.deviceOS = deviceOS;
    }

    public String getDeviceOSVersion() {
        return deviceOSVersion;
    }

    public void setDeviceOSVersion(String deviceOSVersion) {
        this.deviceOSVersion = deviceOSVersion;
    }

    // Optional: Override toString for easier logging/printing
    @Override
    public String toString() {
        return "LogModel{" +
                "logID='" + logID + '\'' +
                ", level=" + level +
                ", type=" + type +
                ", message='" + message + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", sessionID='" + sessionID + '\'' +
                ", loggedAt=" + loggedAt +
                ", freeRAMMB=" + freeRAMMB +
                ", freeStorageMB=" + freeStorageMB +
                ", deviceModel='" + deviceModel + '\'' +
                ", deviceOS='" + deviceOS + '\'' +
                ", deviceOSVersion='" + deviceOSVersion + '\'' +
                '}';
    }
}