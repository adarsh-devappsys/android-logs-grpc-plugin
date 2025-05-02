package com.devappsys.grpc_logs.api;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
public class DeviceInfo {
    private static final String TAG = DeviceInfo.class.getName();
    public static final String OS_NAME = "android";
    private Context context;
    private DeviceInfoCache deviceInfoCache;

    /**
     * Internal class to hold device information as a cache.
     */
    private class DeviceInfoCache {
       private String versionName;
       private String osName;
       private String osVersion;
        private String brand;
        private String manufacturer;
        private String model;
        private String carrier;
        private String packageName;

        DeviceInfoCache (){
            versionName =getVersionName();
            osName=getOsName();
            osVersion=getOsVersion();
            brand=getBrand();
            manufacturer=getManufacturer();
            model=getModel();
            carrier=getCarrier();
            packageName=getPackageName();
        }

        private String getPackageName() {
            return context.getPackageName();
        }
        private String getVersionName() {
            PackageInfo packageInfo;
            try {
                packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                return packageInfo.versionName;
            } catch (Exception ignored) {

            }
            return null;
        }

        private String getOsName() {
            return OS_NAME;
        }

        private String getOsVersion() {
            return Build.VERSION.RELEASE;
        }

        private String getBrand() {
            return Build.BRAND;
        }

        private String getManufacturer() {
            return Build.MANUFACTURER;
        }

        private String getModel() {
            return Build.MODEL;
        }

        private String getCarrier() {
            try {
                TelephonyManager manager = (TelephonyManager) context
                        .getSystemService(Context.TELEPHONY_SERVICE);
                return manager.getNetworkOperatorName();
            } catch (Exception e) {
                // Failed to get network operator name from network
            }
            return null;
        }

    }

    private DeviceInfoCache getDeviceInfoCache(){
        if (deviceInfoCache == null) {
            deviceInfoCache = new DeviceInfoCache();
        }
        return deviceInfoCache;
    }

    public DeviceInfo(Context context) {
        this.context = context;
    }

    public String getVersionName() {
        return getDeviceInfoCache().versionName;
    }
    public String getOsName() {
        return getDeviceInfoCache().osName;
    }
    public String getOsVersion() {
        return getDeviceInfoCache().osVersion;
    }
    public String getBrand() {
        return getDeviceInfoCache().brand;
    }
    public String getManufacturer() {
        return getDeviceInfoCache().manufacturer;
    }
    public String getModel() {
        return getDeviceInfoCache().model;
    }
    public String getCarrier() {
        return getDeviceInfoCache().carrier;
    }
    public String getPackageName() {
        return getDeviceInfoCache().packageName;
    }


}
