package fr.abitbol.service4night;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.abitbol.service4night.services.Service;

public class LocationBuilder {


    public static Location build(Map<String, Object> data){
        return new Location((double)data.get("latitude"),(double)data.get("longitude"),(String) data.get("description"),(HashMap<String, Service>) data.get("services"));
    }
    public static String generateId(LatLng point){
        return String.format(Locale.FRANCE,Double.toString(point.latitude)+'|'+ point.longitude);
    }
//    public static Location build(Marker marker){
//
//    }
}
