/*
 * Nom de classe : DatabaseService
 *
 * Description   : Intent service récuperant les lieux dans la base de données
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.DAO;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QuerySnapshot;


public class DatabaseService extends IntentService {

    private static final String TAG = "DataBaseBindService logging";
    private static OnCompleteListener<QuerySnapshot> listener;
    public static final String ACTION_GET_LOCATIONS = "getLocations";



    private static final String EXTRA_COLLECTION_NAME = "collection";

    public DatabaseService() {
        super("DatabaseService");
    }


    // démarre le service
    public static void startService(Context context, OnCompleteListener<QuerySnapshot> _listener){
        listener = _listener;
        Intent getLocationsIntent = new Intent(context, DatabaseService.class);
        getLocationsIntent.setAction(DatabaseService.ACTION_GET_LOCATIONS);
        context.startService(getLocationsIntent);

    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent called ");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_GET_LOCATIONS.equals(action)) {
                Log.i(TAG, "onHandleIntent: action is get locations");
                final String collection = intent.getStringExtra(EXTRA_COLLECTION_NAME);

                if (listener != null) {
                    Log.i(TAG, "onHandleIntent: context is valid");
                    getLocations(listener);
                }
                else{
                    Log.i(TAG, "onHandleIntent: mapsActivity is null");
                }
            }
        }
    }



    public void getLocations(OnCompleteListener<QuerySnapshot> caller) {
        Log.i(TAG, "getLocations called");
        DAOFactory.getLocationDAOOnline().selectAll(caller);
    }



}