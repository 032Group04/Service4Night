package fr.abitbol.service4night.DAO;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.MapLocation;

public class FirestoreLocationDAO implements LocationDAO {
    private static final String collection = "locations";
    private final String TAG = "FirestoreLocationDAO logging";

    private final FirebaseFirestore dataBase;


    private FirestoreLocationDAO(){



        dataBase = FirebaseFirestore.getInstance();
    }
    public static FirestoreLocationDAO getInstance(){
        return Holder.instance;
    }

    @Override
    public List<MapLocation> selectAll(OnCompleteListener<QuerySnapshot> listener) {

            if (listener != null){

                dataBase.collection(collection)
                        .get()
                        .addOnCompleteListener(listener);

            }else{

                List<MapLocation> visibleLocations = new ArrayList<>();
                dataBase.collection(collection)
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                for (QueryDocumentSnapshot doc : task.getResult()){

                                    Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                                    visibleLocations.add(MapLocation.Builder.build(doc.getData()));
                                }

                            }
                            else{
                                Log.i(TAG, "onComplete: error while getting document");
                            }
                        });
                return visibleLocations;
            }

        return null;

    }

    @Override
    public MapLocation select(String id, OnCompleteListener<DocumentSnapshot> listener) {
        if (listener == null) {
            AtomicReference<DocumentSnapshot> result = new AtomicReference<>();
            dataBase.collection(collection).document(id).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        String description = (String) task.getResult().getData().get("description");
                        Log.i(TAG, "getData: String description value :" + description);
                        result.set(task.getResult());
                    }
                }
            });

            return MapLocation.Builder.build(result.get().getData());
        }
        else{
            dataBase.collection(collection).document(id).get().addOnCompleteListener(listener);
        }
        return null;
    }



    @Override
    public boolean remove(String id, OnCompleteListener<Void> listener) {
        if (listener != null) {
            Log.i(TAG, "remove: listener is valid");
            dataBase.collection(collection).document(id).delete().addOnCompleteListener(listener);
        }
        else {
            Log.i(TAG, "remove: no listener");
            //TODO hors ligne
        }
        return false;
    }



    @Override
    public boolean update(String id, MapLocation obj, OnCompleteListener<Void> listener) {
            if (listener != null){

            Map<String,Object> mappedLocation = new HashMap<>();
            mappedLocation.put(LocationDAO.LATITUDE_KEY, obj.getPoint().latitude);
            mappedLocation.put(LocationDAO.LONGITUDE_KEY, obj.getPoint().longitude);
            mappedLocation.put(LocationDAO.LOCATION_ID_KEY, obj.getId());
            mappedLocation.put(LocationDAO.DESCRIPTION_KEY, obj.getDescription());
            mappedLocation.put(LocationDAO.SERVICES_KEY, obj.getServices());
            mappedLocation.put(LocationDAO.USER_ID_KEY, obj.getUser_id());
            mappedLocation.put(LocationDAO.LOCATION_NAME_KEY, obj.getName());
            if (obj.getPictures() != null) mappedLocation.put(LocationDAO.PICTURES_URI_KEY,obj.getPictures());
            mappedLocation.put(LocationDAO.CONFIRMED_KEY, obj.isConfirmed());
            dataBase.collection(collection).document(id).update(mappedLocation).addOnCompleteListener(listener);

        } else {
            AtomicBoolean success = new AtomicBoolean(false);
            Map<String,Object> mappedLocation = new HashMap<>();
            mappedLocation.put(LocationDAO.LATITUDE_KEY, obj.getPoint().latitude);
            mappedLocation.put(LocationDAO.LONGITUDE_KEY, obj.getPoint().longitude);
            mappedLocation.put(LocationDAO.LOCATION_ID_KEY, obj.getId());
            mappedLocation.put(LocationDAO.DESCRIPTION_KEY, obj.getDescription());
            mappedLocation.put(LocationDAO.SERVICES_KEY, obj.getServices());
            mappedLocation.put(LocationDAO.USER_ID_KEY, obj.getUser_id());
            mappedLocation.put(LocationDAO.LOCATION_NAME_KEY, obj.getName());
            if (obj.getPictures() != null) mappedLocation.put(LocationDAO.PICTURES_URI_KEY,obj.getPictures());
            mappedLocation.put(LocationDAO.CONFIRMED_KEY, obj.isConfirmed());
            dataBase.collection(collection).document(id).update(mappedLocation).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    success.set(true);
                    Log.i(TAG, "onComplete: location successfully written. ");
                }
                else{
                    Log.i(TAG, "onComplete: location failed to be written");
                    Log.i(TAG, "onComplete: task to string : " + task.toString());
                    Log.i(TAG, "onComplete: task get Exception : "+ task.getException());
                }
            });
            return success.get();
        }
        return false;
    }



    @Override
    public boolean insert(MapLocation obj, OnCompleteListener<Void> listener) {
        Map<String,Object> mappedLocation = new HashMap<>();
        mappedLocation.put(LocationDAO.LATITUDE_KEY, obj.getPoint().latitude);
        mappedLocation.put(LocationDAO.LONGITUDE_KEY, obj.getPoint().longitude);
        mappedLocation.put(LocationDAO.LOCATION_ID_KEY, obj.getId());
        mappedLocation.put(LocationDAO.DESCRIPTION_KEY, obj.getDescription());
        mappedLocation.put(LocationDAO.SERVICES_KEY, obj.getServices());
        mappedLocation.put(LocationDAO.USER_ID_KEY, obj.getUser_id());
        mappedLocation.put(LocationDAO.LOCATION_NAME_KEY, obj.getName());
        if (obj.getPictures() != null) mappedLocation.put(LocationDAO.PICTURES_URI_KEY,obj.getPictures());
        mappedLocation.put(LocationDAO.CONFIRMED_KEY, obj.isConfirmed());
        if (listener != null) {
            Log.i(TAG, "insert: listened insert");

            // voir firestore storage pour les photos et checker quelles classes sont serialisables
            dataBase.collection("locations").document(obj.getId())
                    .set(mappedLocation)
                    .addOnCompleteListener(listener);
        }
        else {
            Log.i(TAG, "insert: not listened insert");
            AtomicBoolean success = new AtomicBoolean(false);
            
            // voir firestore storage pour les photos et checker quelles classes sont serialisables
            dataBase.collection("locations").document(obj.getId())
                    .set(mappedLocation)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            success.set(true);
                            Log.i(TAG, "onComplete: location successfully written. ");
                        } else {
                            Log.i(TAG, "onComplete: location failed to be written");
                            Log.i(TAG, "onComplete: task to string : " + task.toString());
                            Log.i(TAG, "onComplete: task get Exception : " + task.getException());
                        }
                    });
            return success.get();
        }
        return false;
    }
    private static class Holder{
        private final static FirestoreLocationDAO instance = new FirestoreLocationDAO();
    }

}
