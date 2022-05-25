package fr.abitbol.service4night;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.QuerySnapshot;

import fr.abitbol.service4night.DAO.DAOFactory;


public class DatabaseService extends IntentService {

    private static final String TAG = "DataBaseBindService logging";
//    private IBinder binderInterface;
    private static OnCompleteListener<QuerySnapshot> listener;
    public static final String ACTION_GET_LOCATIONS = "getLocations";


    // TODO: Rename parameters
    private static final String EXTRA_COLLECTION_NAME = "collection";

    public DatabaseService() {
        super("DatabaseService");
    }

//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        Log.i(TAG, "onBind called");
//        binderInterface = new ServiceBinder();
//        return binderInterface;
//    }

    public static void startService(Context context, OnCompleteListener<QuerySnapshot> _listener){
        listener = _listener;
        Intent getLocationsIntent = new Intent(context, DatabaseService.class);
        getLocationsIntent.setAction(DatabaseService.ACTION_GET_LOCATIONS);
        context.startService(getLocationsIntent);
//                    bindService(getLocationsIntent, this, BIND_AUTO_CREATE);

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
//    public boolean isServiceBound(){
//        Log.i(TAG, "isServiceBound? yes");
//        return true;
//    }
//
//    public void stopBindService(){
//
//        Log.i(TAG, "stopBindService called");
//        stopService(new Intent(this,DatabaseService.class));
//    }


    public void getLocations(OnCompleteListener<QuerySnapshot> caller) {
        Log.i(TAG, "getLocations called");
        DAOFactory.getLocationDAOOnline().selectAll(caller);
    }
//    public class ServiceBinder extends Binder {
//        public DatabaseService getBoundService(OnCompleteListener<QuerySnapshot> caller){
//            mapsActivity = (MapsActivity) caller;
//            return DatabaseService.this;
//        }
//
//    }


}