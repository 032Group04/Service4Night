/*
 * Nom de classe : NetworkReceiver
 *
 * Description   : Broadcast receiver sur l'état du réseau
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.utils;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;

import fr.abitbol.service4night.MainActivity;

public class NetworkReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkReceiver logging";
    private NetworkInfo networkInfo;
    private MainActivity mainActivity;


    private NetworkReceiver(){

    }
    public static NetworkReceiver getInstance(){
        return Holder.instance;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: network changed");
        ConnectivityManager connManager = context.getSystemService(ConnectivityManager.class);
        networkInfo = connManager.getActiveNetworkInfo();

        if (mainActivity == null && context instanceof MainActivity){
            Log.i(TAG, "onReceive: context is mainActivity");
            mainActivity = (MainActivity) context;
        }
        if (mainActivity != null) {
            if (networkInfo == null || !networkInfo.isConnected()|| !networkInfo.isAvailable()) {
                Log.i(TAG, "onReceive: network unavailable");

                mainActivity.showNetworkError();

            } else {
                Log.i(TAG, "onReceive: network available");
                if (mainActivity.getNetworkAlertDialog() != null) {
                    mainActivity.getNetworkAlertDialog().cancel();
                }
            }
        }

    }

    public NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public static class Holder{
        private static NetworkReceiver instance = new NetworkReceiver();
    }
}