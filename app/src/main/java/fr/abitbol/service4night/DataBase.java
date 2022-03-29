package fr.abitbol.service4night;


import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class DataBase  {
    private final String TAG = "logging from Database";
    FirebaseFirestore dataBase;
    public DataBase(){
        dataBase = FirebaseFirestore.getInstance();
    }

    public void getData(String collection){
        //DocumentReference document = dataBase.collection("locations").document("2bvxd9pTZk8xh9aDwbHQ");
        dataBase.collection(collection)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            for (QueryDocumentSnapshot doc : task.getResult()){
                                Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                            }
                        }
                        else{
                            Log.i(TAG, "onComplete: error' getting document");
                        }
                    }
                });
    }
    public void registerLocation(Location location){
        Map<String,Object> mappedLocation = new HashMap<>();
        mappedLocation.put("latitude",location.getPoint().latitude);
        mappedLocation.put("longitude",location.getPoint().longitude);
        mappedLocation.put("id",location.getId());
        mappedLocation.put("description",location.getDescription());
        // voir firestore storage pour les photos et checker quelles classes sont serialisableq
        dataBase.collection("locations").document()
                .set(mappedLocation)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.i(TAG, "onSuccess: location succesfully written");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "onFailure: location failed to be written");

                    }
                });


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
