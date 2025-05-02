package com.devappsys.grpc_logs.util;

public class EmulatorUtil {
    public static boolean isEmulator() {
        String fingerprint = android.os.Build.FINGERPRINT;
        String model = android.os.Build.MODEL;
        String brand = android.os.Build.BRAND;
        String device = android.os.Build.DEVICE;
        String product = android.os.Build.PRODUCT;
        String manufacturer = android.os.Build.MANUFACTURER;

        return fingerprint.startsWith("generic")
                || fingerprint.toLowerCase().contains("vbox")
                || fingerprint.toLowerCase().contains("test-keys")
                || model.contains("google_sdk")
                || model.contains("Emulator")
                || model.contains("Android SDK built for x86")
                || manufacturer.contains("Genymotion")
                || brand.startsWith("generic") && device.startsWith("generic")
                || "google_sdk".equals(product);
    }
}
