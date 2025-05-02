package com.devappsys.grpc_logs.api.listeners;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private final NetworkChangeCallback callback;

    // Constructor accepting a callback
    public NetworkChangeReceiver(NetworkChangeCallback callback) {
        this.callback = callback;
    }

    @androidx.annotation.RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE
    })
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);

        boolean isInternetAvailable = networkCapabilities != null &&
                (networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED));

        if (networkCapabilities != null) {
            String networkType = "";
            String carrierDetails = "";

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkType = "Wi-Fi";
                carrierDetails = "N/A";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                if (telephonyManager != null) {
                    String carrierName = telephonyManager.getNetworkOperatorName();
                    String carrierId = telephonyManager.getNetworkOperator();
                    carrierDetails = carrierName;
                    networkType = carrierId;
                } else {
                    carrierDetails = "Mobile Data - Carrier Unknown";
                    networkType = "Mobile Data";
                }
            }

            // ✅ Pass internet availability in the callback
            callback.onNetworkChange(carrierDetails, networkType, isInternetAvailable);
        }
    }

    // Callback interface for notifying the network change with all data
    public interface NetworkChangeCallback {
        void onNetworkChange(String carrierName, String carrierID, boolean isInternetAvailable);
    }
}