package com.devappsys.logs_grpc.models.data;

import com.devappsys.log.ContextOuterClass;

public class ContextModel {

    private String deviceID;
    private String appVersion;
    private String appPackageName;
    private String userID;
    private String sessionID;
    private String language;
    private String networkStatus;
    private String location;
    private String ipAddress;
    private boolean isRooted;

    // Constructor
    public ContextModel(String deviceID, String appVersion, String appPackageName, String userID,
                        String sessionID, String language, String networkStatus, String location,
                        String ipAddress, boolean isRooted) {
        this.deviceID = deviceID;
        this.appVersion = appVersion;
        this.appPackageName = appPackageName;
        this.userID = userID;
        this.sessionID = sessionID;
        this.language = language;
        this.networkStatus = networkStatus;
        this.location = location;
        this.ipAddress = ipAddress;
        this.isRooted = isRooted;
    }

    // Method to convert ContextModel to Protobuf Context
    public ContextOuterClass.Context toProtobuf() {
        return ContextOuterClass.Context.newBuilder()
                .setDeviceID(this.deviceID)
                .setAppVersion(this.appVersion)
                .setAppPackageName(this.appPackageName)
                .setUserID(this.userID)
                .setSessionID(this.sessionID)
                .setLanguage(this.language)
                .setNetworkStatus(this.networkStatus)
                .setLocation(this.location)
                .setIpAddress(this.ipAddress)
                .setIsRooted(this.isRooted)
                .build();
    }

    // Getters and Setters

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getNetworkStatus() {
        return networkStatus;
    }

    public void setNetworkStatus(String networkStatus) {
        this.networkStatus = networkStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isRooted() {
        return isRooted;
    }

    public void setRooted(boolean rooted) {
        isRooted = rooted;
    }
}