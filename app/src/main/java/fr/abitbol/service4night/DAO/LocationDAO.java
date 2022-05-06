package fr.abitbol.service4night.DAO;

import com.google.firebase.firestore.FirebaseFirestore;

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

    public abstract List<MapLocation> selectAll();
    public abstract MapLocation select(String id);
    public abstract boolean remove(String id);
    public abstract boolean update(String id,MapLocation obj);
    public abstract boolean insert(MapLocation obj);

}
