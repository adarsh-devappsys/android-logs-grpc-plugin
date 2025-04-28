package com.devappsys.logs_grpc.models;

public class Configuration {
    private String host;
    private int port;
    private String packageName;
    private String clientId;
    private String manufacturer;
    private String model;
    private String osVersion;
    private String appVersion;
    private String deviceId;
    private String userId;


    public Configuration(String host, int port, String packageName, String clientId, String manufacturer, String model, String osVersion, String appVersion, String deviceId, String userId) {
        this.host = host;
        this.port = port;
        this.packageName = packageName;
        this.clientId = clientId;
        this.manufacturer = manufacturer;
        this.model = model;
        this.osVersion = osVersion;
        this.appVersion = appVersion;
        this.deviceId = deviceId;
        this.userId = userId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
