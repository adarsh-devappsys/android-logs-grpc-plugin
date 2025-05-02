package com.devappsys.grpc_logs.util;

import android.util.Log;

public class Logger {
    private static final String TAG = "GRPC_LOGS";
    private boolean isDebug = true;

    public Logger(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public void setDebug(boolean debug) {
        isDebug = debug;
    }

    public void d(String message) {
        if (isDebug) {
            Log.d(TAG, message);
        }
    }

    public void e(String message) {
        if (isDebug) {
            Log.e(TAG, message);
        }
    }

    public void i(String message) {
        if (isDebug) {
            Log.i(TAG, message);
        }
    }

    public void w(String message) {
        if (isDebug) {
            Log.w(TAG, message);
        }
    }
}
