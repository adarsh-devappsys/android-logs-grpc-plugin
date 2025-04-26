package com.devappsys.logs_grpc;

import com.devappsys.logs_grpc.models.Configuration;

public class LogsPlugin {
    private static LogsPlugin _instance;


    public static LogsPlugin getInstance() {
        if (_instance == null) {
            throw new IllegalStateException("LogsPlugin is not initialized");
        }
        return _instance;
    }
    private LogsPlugin(Configuration configuration){
        this.configuration = configuration;
    }
    private Configuration configuration;

    public static void init(Configuration configuration) {
        if (_instance != null) {
            throw new IllegalStateException("LogsPlugin is already initialized");
        }
        _instance = new LogsPlugin(configuration);
    }

}
