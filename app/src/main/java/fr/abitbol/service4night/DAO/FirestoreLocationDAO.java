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
    private final OnCompleteListener<?> onCompleteListener;
    private enum Type { select, insert}
    private Type type;
    private boolean listened;
    //TODO voir transformer en singleton
    public FirestoreLocationDAO(OnCompleteListener<QuerySnapshot> context){
        Log.i(TAG, "FirestoreLocationDAO: select constructor called");
        type = Type.select;
        onCompleteListener = context;
        dataBase = FirebaseFirestore.getInstance();
    }
    //TODO supprimer listened et créer listeners locaux
    public FirestoreLocationDAO(OnCompleteListener<Void> context,boolean _listened){
        onCompleteListener = context;
        listened = _listened;
        type =Type.insert;
        if ( onCompleteListener == null){
            listened = false;
        }
        Log.i(TAG, "FirestoreLocationDAO: insert constructor called , listened is : "+listened);
        dataBase = FirebaseFirestore.getInstance();


    }
    @Override
    public List<MapLocation> selectAll() {


        switch (type){
            case select:{

                dataBase.collection(collection)
                        .get()
                        .addOnCompleteListener((OnCompleteListener<QuerySnapshot>) onCompleteListener);

            }break;
            case insert:{
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
        }
        return null;

    }


    @Override
    public MapLocation select(String id) {

        AtomicReference<DocumentSnapshot> result = new AtomicReference<>();
        dataBase.collection(collection).document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                if(task.getResult() != null) {
                    String description = (String) task.getResult().getData().get("description");
                    Log.i(TAG, "getData: String description value :" + description);
                    result.set(task.getResult());
                }
            }
        });

        return MapLocation.Builder.build(result.get().getData());
    }


    @Override
    public boolean remove(String id) {
        return false;
    }

    @Override
    public boolean update(String id, MapLocation obj) {
        if (type == Type.insert && listened) {

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
            dataBase.collection(collection).document(id).update(mappedLocation).addOnCompleteListener((OnCompleteListener<Void>) onCompleteListener);

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
    public void update(String id, MapLocation obj, OnCompleteListener<Void> listener) {


    }

    @Override
    public boolean insert(MapLocation obj) {
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
        if (type == Type.insert && listened) {
            Log.i(TAG, "insert: listened insert");

            // voir firestore storage pour les photos et checker quelles classes sont serialisables
            dataBase.collection("locations").document(obj.getId())
                    .set(mappedLocation)
                    .addOnCompleteListener((OnCompleteListener<Void>) onCompleteListener);
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

}
