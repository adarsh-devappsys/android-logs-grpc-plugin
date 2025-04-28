package com.devappsys.logs_grpc.listener;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private NetworkChangeCallback callback;

    // Constructor accepting a callback
    public NetworkChangeReceiver(NetworkChangeCallback callback) {
        this.callback = callback;
    }

    @androidx.annotation.RequiresPermission(allOf = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE // for full carrier info (optional extra)
    })
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Network activeNetwork = cm.getActiveNetwork();
        NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);

        if (networkCapabilities != null) {
            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                callback.onNetworkChange("Wi-Fi");
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                // Cellular network (SIM card)
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                if (telephonyManager != null) {
                    String carrierName = telephonyManager.getNetworkOperatorName();  // Example: "Airtel" or "Jio"
                    String carrierId = telephonyManager.getNetworkOperator();       // Example: "40410" (MCC+MNC code)

                    String networkDetails = "Mobile Data - Carrier: " + carrierName + ", Carrier ID: " + carrierId;
                    callback.onNetworkChange(networkDetails);
                } else {
                    callback.onNetworkChange("Mobile Data - Carrier Unknown");
                }
            }
        }
    }

    // Callback interface for notifying the network change
    public interface NetworkChangeCallback {
        void onNetworkChange(String networkType);
    }
}