package com.devappsys.logs_grpc.util;

import android.os.Environment;
import android.os.StatFs;

public class StorageUtil {

    // Method to get free storage in bytes (internal memory)
    public static long getFreeStorage() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());

        // For SDK 18 and above
        return statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
    }

    // Method to get total storage in bytes (internal memory)
    public static long getTotalStorage() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());

        // For SDK 18 and above
        return statFs.getBlockCountLong() * statFs.getBlockSizeLong();
    }
}