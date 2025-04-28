package com.devappsys.logs_grpc.util;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MemoryUtil {

    // This method will return the available free RAM in bytes
    public static long getFreeMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

        // Check if the device is running on a version of Android that supports the required API
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.availMem; // Return available memory in bytes
    }

    // This method will return the total RAM size in bytes
    public static long getTotalMemory() {
        String filePath = "/proc/meminfo";
        String line;
        long totalMemory = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("MemTotal")) {
                    // The value is in kilobytes, so we multiply by 1024 to convert to bytes
                    totalMemory = Long.parseLong(line.split(":")[1].trim().split(" ")[0]) * 1024;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return totalMemory;
    }
}