package com.devappsys.grpc_logs.api;

public class Configuration {
    private String host;
    private int port;
    private String deviceId;
    private String userId;
    private long batchSize;
    private int appId;

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public Configuration(String host, int port, String deviceId, int appId, String userId, long batchSize) {
        this.host = host;
        this.port = port;
        this.deviceId = deviceId;
        this.userId = userId;
        this.appId=appId;
        this.batchSize = batchSize;
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(long batchSize) {
        this.batchSize = batchSize;
    }
}
