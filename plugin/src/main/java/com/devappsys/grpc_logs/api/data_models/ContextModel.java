package com.devappsys.grpc_logs.api.data_models;

import com.devappsys.log.Context;

import java.util.UUID;

public class ContextModel {
    private String contextID;
    private String sessionId;
    private String userID;

    // Device Info
    private String deviceID;
    private String deviceType;
    private String deviceFamily;
    private String deviceCarrier;

    // OS and app info

    private String platform;
    private String osName;
    private String osVersion;
    private String appId;
    private String appVersion;
    private String startVersion;
    private String sdkLibrary;

    // location and locales
    private String language;
    private String ipAddress;
    private String city;
    private String region;
    private String country;
    private double latitude;
    private double longitude;

    // Constructor


    public ContextModel( String sessionId, String userID, String deviceID, String deviceType, String deviceFamily, String deviceCarrier, String platform, String osName, String osVersion, String appId, String appVersion, String startVersion, String sdkLibrary, String language, String ipAddress, String city, String region, String country, double latitude, double longitude) {
        this.contextID = UUID.randomUUID().toString();
        this.sessionId = sessionId;
        this.userID = userID;
        this.deviceID = deviceID;
        this.deviceType = deviceType;
        this.deviceFamily = deviceFamily;
        this.deviceCarrier = deviceCarrier;
        this.platform = platform;
        this.osName = osName;
        this.osVersion = osVersion;
        this.appId = appId;
        this.appVersion = appVersion;
        this.startVersion = startVersion;
        this.sdkLibrary = sdkLibrary;
        this.language = language;
        this.ipAddress = ipAddress;
        this.city = city;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Method to convert ContextModel to Protobuf Context
    public Context.ContextMessage toProtobuf() {
        return Context.ContextMessage.newBuilder()
                .setContextId(contextID)
                .setDeviceId(this.deviceID)
                .setAppVersion(this.appVersion)
                .setUserId(userID)
                .setSessionId(this.sessionId)
                .setDeviceId(deviceID)
                .setDeviceType(this.deviceType)
                .setDeviceFamily(deviceFamily)
                .setDeviceCarrier(deviceCarrier)
                .setPlatform(platform)
                .setOsName(osName)
                .setOsVersion(osVersion)
                .setAppId(appId)
                .setStartVersion(startVersion)
                .setSdkLibrary(sdkLibrary)
                .setLanguage(language)
                .setIpAddress(ipAddress)
                .setCity(city)
                .setRegion(region)
                .setCountry(country)
                .setLocationLat(latitude)
                .setLocationLng(longitude)
                .build();
    }

    // Getters and Setters

    public String getContextID() {
        return contextID;
    }

    public void setContextID(String contextID) {
        this.contextID = contextID;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getDeviceFamily() {
        return deviceFamily;
    }

    public void setDeviceFamily(String deviceFamily) {
        this.deviceFamily = deviceFamily;
    }

    public String getDeviceCarrier() {
        return deviceCarrier;
    }

    public void setDeviceCarrier(String deviceCarrier) {
        this.deviceCarrier = deviceCarrier;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getStartVersion() {
        return startVersion;
    }

    public void setStartVersion(String startVersion) {
        this.startVersion = startVersion;
    }

    public String getSdkLibrary() {
        return sdkLibrary;
    }

    public void setSdkLibrary(String sdkLibrary) {
        this.sdkLibrary = sdkLibrary;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String isCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}