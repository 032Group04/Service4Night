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

    //TODO: lancer dans un service
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

    public Map<String, Object> parseServices(Location location){
        Map<String,Object> servicesMap = new HashMap<>();
        location.getServices().forEach((s, service) -> {

        });
        return servicesMap;

    }

    public void registerLocation(Location location,LocationAddFragment caller){
        //TODO: gérer accès hors ligne, soit ajouter aux sharedPreferences soit voir version hors ligne firebase
        Map<String,Object> mappedLocation = new HashMap<>();
        mappedLocation.put("latitude",location.getPoint().latitude);
        mappedLocation.put("longitude",location.getPoint().longitude);
        mappedLocation.put("id",location.getId());
        mappedLocation.put("description",location.getDescription());
        mappedLocation.put("services",location.getServices());
        mappedLocation.put("user_id",location.getUser_id());
        mappedLocation.put("name",location.getName());
        mappedLocation.put("confirmed",location.isConfirmed());

        // voir firestore storage pour les photos et checker quelles classes sont serialisables
        dataBase.collection("locations").document(location.getId())
                .set(mappedLocation)
                .addOnCompleteListener(caller);


    }

}
