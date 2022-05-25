package fr.abitbol.service4night.DAO;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import fr.abitbol.service4night.MapLocation;

public interface LocationDAO {
    public static final String LATITUDE_KEY = "latitude";
    public static final String LONGITUDE_KEY = "longitude";
    public static final String USER_ID_KEY = "user_id";
    public static final String DESCRIPTION_KEY = "description";
    public static final String SERVICES_KEY = "services";
    public static final String LOCATION_NAME_KEY = "name";
    public static final String CONFIRMED_KEY = "confirmed";
    public static final String LOCATION_ID_KEY = "id";
    public static final String PICTURES_URI_KEY = "pictures";


    List<MapLocation> selectAll(OnCompleteListener<QuerySnapshot> listener);

    MapLocation select(String id, OnCompleteListener<DocumentSnapshot> listener);

    public boolean remove(String id, OnCompleteListener<Void> listener);

    boolean update(String id, MapLocation obj, OnCompleteListener<Void> listener);


    boolean insert(MapLocation obj, OnCompleteListener<Void> listener);
}
