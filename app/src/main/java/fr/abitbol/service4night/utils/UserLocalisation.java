/*
 * Nom de classe : UserLocalisation
 *
 * Description   : localise l'utilisateur
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
import android.content.Context;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.listeners.OnCompleteLocalisationListener;

public class UserLocalisation  {
    private static final String TAG = "UserLocalisation logging";
    private FusedLocationProviderClient locationProviderClient;
    private OnCompleteLocalisationListener listener;
    public UserLocalisation(Context context){
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }
// L'IDE pense naîvement pouvoir régler le problème de permission avec quelques lignes dans le MANIFEST
    @SuppressLint("MissingPermission")

    public synchronized void locateUser(OnCompleteLocalisationListener _listener){
        Log.i(TAG, "locateUser: called");
        if (MainActivity.coarseLocation || MainActivity.fineLocation) {
            Log.i(TAG, "locateUser: localisation is available");
            listener = _listener;
            new Thread(() ->{
                Log.i(TAG, "locateUser: called");
                locationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                    listener.onCompleteLocation(task);
                });
            } ).start();
            Log.i(TAG, "locateUser: location thread started");

        }
        else{ Log.i(TAG, "locateUser: no location permissions available"); }
    }







}
