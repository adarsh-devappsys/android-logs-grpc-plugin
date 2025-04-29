package com.devappsys.logs_grpc.listener;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

import com.devappsys.logs_grpc.models.data.ContextModel;  // Assuming ContextModel is your log context model

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
            String networkType = "";
            String carrierDetails = "";

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                networkType = "Wi-Fi";
                carrierDetails = "N/A";
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                // Cellular network (SIM card)
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                if (telephonyManager != null) {
                    String carrierName = telephonyManager.getNetworkOperatorName();  // Example: "Airtel" or "Jio"
                    String carrierId = telephonyManager.getNetworkOperator();       // Example: "40410" (MCC+MNC code)

                    carrierDetails = carrierName ;
                    networkType = carrierId;
                } else {
                    carrierDetails = "Mobile Data - Carrier Unknown";
                    networkType = "Mobile Data";
                }
            }


            // Trigger callback with the ContextModel, network type, and carrier details
            callback.onNetworkChange(carrierDetails, networkType);
        }
    }

    // Callback interface for notifying the network change with all data
    public interface NetworkChangeCallback {
        void onNetworkChange(String carrierName, String carrierID);
    }
}