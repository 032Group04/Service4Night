package fr.abitbol.service4night;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.abitbol.service4night.DAO.LocationDAO;
import fr.abitbol.service4night.services.Service;

public class MapLocation {

    private String description;
    private String id;
    private String name;
    private LatLng point;
    private List<Uri> pictures;
    private Map<String, Service> services;
    private String user_id;
    private boolean confirmed;
    private static final String TAG = "MapLocation logging";

    //TODO : créer classe dédiée a la localisation
    public MapLocation(double latitude, double longitude, String _description, Map<String,Service> _services, String _user_id, String _name,List<Uri> _pictures, boolean _confirmed)  {
        point = new LatLng(latitude, longitude);
        services = _services;
        description = _description;
        id = String.format(Locale.ENGLISH,Double.toString(latitude)+'|'+ longitude);
        user_id = _user_id;
        name = _name;
        pictures = _pictures;
        confirmed = _confirmed;

    }

    public MapLocation(LatLng _point, String _description, Map<String,Service> _services, String _user_id, String _name, List<Uri> _pictures, boolean _confirmed) {
        point = _point;
        services = _services;
        user_id = _user_id;
        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(point.latitude)+'|'+ point.longitude);
        name = _name;
        pictures = _pictures;
        confirmed = _confirmed;
    }



    public void addService(Service service){
        if (services.containsKey(service.getLabel())){
            services.replace(service.getLabel(),service);
        }
        else {
            services.put(service.getLabel(), service);
        }
    }

    public Map<String,Object> getMappedLocation(){
        Map<String,Object> locationMap = new HashMap<>();
        locationMap.put(id,this);
        return locationMap;

    }

    public boolean hasService(String label){
        return services.containsKey(label);
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj){
            return true;
        }
        if (!(obj instanceof MapLocation)){
            return false;
        }
        else{
            return id.equals(((MapLocation) obj).getId());
        }


    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }

    public Map<String, Service> getServices() {
        return services;
    }


    public boolean isLocationInArea (LatLngBounds bounds){
          return bounds.contains(point);
    }

    public String getUser_id() {
        return user_id;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

//    public Uri getUri(){
//        return picture;
//    }

    public List<Uri> getPictures() {
        return pictures;
    }

    public void setPictures(List<Uri> pictures) {
        this.pictures = pictures;
    }

    public void setServices(Map<String, Service> services) {
        this.services = services;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getName() {
        return name;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public LatLng getPoint() {
        return point;
    }
    public static class Builder {
        private static final String TAG = "LocationBuilder logging";


        public static MapLocation build(Map<String,Object> data) throws NullPointerException {
            //TODO : créer constantes dans DataBase pour noms des données
            double latitude = (double) data.get(LocationDAO.LATITUDE_KEY);
            Log.i(TAG, "build: lat  =" + latitude);
            double longitude = (double) data.get(LocationDAO.LONGITUDE_KEY);
            Log.i(TAG, "build: lng = " + longitude);
            String description = (String) data.get(LocationDAO.DESCRIPTION_KEY);
            Log.i(TAG, "build: description = " + description);
            List<Uri> pictures = null;
            if(data.containsKey(LocationDAO.PICTURES_URI_KEY)) {
                Uri[] picturesArray =  (Uri[]) data.get(LocationDAO.PICTURES_URI_KEY);
                if (picturesArray != null){
                    pictures = Arrays.asList(picturesArray);
                }
            }

            Map<String, Map<String, Object>> servicesData = (Map<String, Map<String, Object>>) data.get(LocationDAO.SERVICES_KEY);
            Map<String, Service> services = null;
            if (servicesData != null) {
                services = Service.Builder.buildServices(servicesData);
            } else {
                Log.i(TAG, "build: map services data is null");
            }


            return new MapLocation(latitude, longitude, description, services, (String) data.get(LocationDAO.USER_ID_KEY), (String) data.get(LocationDAO.LOCATION_NAME_KEY),pictures, (boolean) data.get(LocationDAO.CONFIRMED_KEY));
        }


        public static String generateId(LatLng point) {

            return String.format(Locale.ENGLISH, Double.toString(point.latitude) + '|' + point.longitude);
        }
    }
}
