package fr.abitbol.service4night;


import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class DataBase  {
    private final String TAG = "logging from Database";
    FirebaseFirestore dataBase;
    public DataBase(){
        dataBase = FirebaseFirestore.getInstance();
    }

    public void callLocations(String collection, MapsActivity caller){

        AtomicReference<ArrayList<Location>> locationsReference = null;
        dataBase.collection(collection)
                .get()
                .addOnCompleteListener(caller);


    }

    public Map<String, Object> getData(String collection, String document){
        DocumentReference docRef = dataBase.collection(collection).document(document);
        AtomicReference<Map<String, Object>> result = null;
        docRef.get().addOnCompleteListener(task ->{
            if (task.isSuccessful()){
                double d = (double) task.getResult().getData().get("latitude");
                Log.i(TAG, "getData: double latitude value: " + d );
                String description = (String) task.getResult().getData().get("description");
                Log.i(TAG, "getData: String description value :"+ description);
                result.set(task.getResult().getData());
            }
        });
        return result.get();
    }
    public void registerLocation(Location location,LocationAddFragment caller){
        Map<String,Object> mappedLocation = new HashMap<>();
        mappedLocation.put("latitude",location.getPoint().latitude);
        mappedLocation.put("longitude",location.getPoint().longitude);
        mappedLocation.put("id",location.getId());
        mappedLocation.put("description",location.getDescription());

        // voir firestore storage pour les photos et checker quelles classes sont serialisables
        dataBase.collection("locations").document()
                .set(mappedLocation)
                .addOnCompleteListener(caller);


    }

    /*
    *   pour firebase realtime database
    */
//        public void registerLocation(Location location){
//        DataBase dataBase = new DataBase("https://service4night-default-rtdb.europe-west1.firebasedatabase.app/");
//
//            dataBase.databaseReference.child("locations").child("water").child(location.getId()).setValue(location);
//
//    }
//    DatabaseReference databaseReference;

//    public DataBase(String URL){
//        databaseReference = FirebaseDatabase.getInstance(URL).getReference();
//    }
//
//    public Location[] getLocations(){
//        Query locationsQuery = databaseReference.equalTo("locations");
//        locationsQuery.
//    }
}
