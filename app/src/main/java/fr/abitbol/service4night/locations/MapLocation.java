/*
 * Nom de classe : MapLocation
 *
 * Description   : représente les lieux
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.locations;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import fr.abitbol.service4night.DAO.LocationDAO;

public class MapLocation implements Parcelable {

    private String description;
    private String id;
    private String name;
    private LatLng point;
    private List<String> pictures;
    private Map<String, Service> services;
    private String user_id;
    private boolean confirmed;
    private static final String TAG = "MapLocation logging";



    public MapLocation(double latitude, double longitude, String _description, Map<String,Service> _services, String _user_id, String _name,List<String> _pictures, boolean _confirmed)  {
        point = new LatLng(latitude, longitude);
        services = _services;
        description = _description;
        id = String.format(Locale.ENGLISH,Double.toString(latitude)+'|'+ longitude);
        user_id = _user_id;
        name = _name;
        pictures = _pictures;
        confirmed = _confirmed;

    }

    public MapLocation(LatLng _point, String _description, Map<String,Service> _services, String _user_id, String _name, List<String> _pictures, boolean _confirmed) {
        point = _point;
        services = _services;
        user_id = _user_id;
        description = _description;
        id = String.format(Locale.ENGLISH,Double.toString(point.latitude)+'|'+ point.longitude);
        name = _name;
        pictures = _pictures;
        confirmed = _confirmed;
    }
    protected MapLocation(Parcel parcel){
        Log.i(TAG, "MapLocation parcel constructor");
        Bundle bundle = parcel.readBundle(getClass().getClassLoader());
        point = new LatLng((double)bundle.getDouble(LocationDAO.LATITUDE_KEY),
                (double)bundle.getDouble(LocationDAO.LONGITUDE_KEY));
        Log.i(TAG, "MapLocation: parcel constructor : build service reached");
        services = Service.Builder.buildServices(bundle.getBundle(LocationDAO.SERVICES_KEY));
        Log.i(TAG, "MapLocation: parcel constructor : build service passed");
        user_id = (String) bundle.getString(LocationDAO.USER_ID_KEY);
        description = (String) bundle.getString(LocationDAO.DESCRIPTION_KEY);
        id = String.format(Locale.ENGLISH,Double.toString(point.latitude)+'|'+ point.longitude);
        name = (String) bundle.getString(LocationDAO.LOCATION_NAME_KEY);
        pictures = bundle.getStringArrayList(LocationDAO.PICTURES_URI_KEY);
        confirmed = (boolean) bundle.getBoolean(LocationDAO.CONFIRMED_KEY);
    }

    // génère un nom pour les photos
    public static String generatePictureName(String locationId, int picturesCount){
        return locationId+"_pic#"+picturesCount+".jpg "; }
    // ajoute un Service
    public void addService(Service service){
        if (services.containsKey(service.getLabel())){
            services.replace(service.getLabel(),service);
        }
        else {
            services.put(service.getLabel(), service);
        }
    }

    // renvoie les services dans une Map
    public Map<Object,Object> getServicesAsMap(){
        Map<Object,Object> mappedServices = new HashMap<>();
        services.forEach((s, service) -> {
           mappedServices.put(service.getLabel(),service.getAsMap());
        });
        return mappedServices;
    }
    // renvoie les services dans un bundle
    public Bundle getServicesAsBundle(){
        Bundle bundle = new Bundle();
        services.forEach(bundle::putParcelable);

        return bundle;
    }

    // renvoie le lieu sous forme de Map
    public Map<Object, Object> getAsMap(){
        Map<Object,Object> mappedLocation = new HashMap<>();
        mappedLocation.put(LocationDAO.LATITUDE_KEY, point.latitude);
        mappedLocation.put(LocationDAO.LONGITUDE_KEY, point.longitude);
        mappedLocation.put(LocationDAO.LOCATION_ID_KEY, id);
        mappedLocation.put(LocationDAO.DESCRIPTION_KEY, description);
        mappedLocation.put(LocationDAO.SERVICES_KEY, services);
        mappedLocation.put(LocationDAO.USER_ID_KEY, user_id);
        mappedLocation.put(LocationDAO.LOCATION_NAME_KEY, name);
        if (pictures != null) mappedLocation.put(LocationDAO.PICTURES_URI_KEY,pictures);
        mappedLocation.put(LocationDAO.CONFIRMED_KEY, confirmed);
        return mappedLocation;
    }
    // renvoie le lieu sous forme de Bundle
    public Bundle getAsBundle(){
        Bundle bundle = new Bundle();
        bundle.putDouble(LocationDAO.LATITUDE_KEY, point.latitude);
        bundle.putDouble(LocationDAO.LONGITUDE_KEY, point.longitude);
        bundle.putString(LocationDAO.LOCATION_ID_KEY, id);
        bundle.putString(LocationDAO.DESCRIPTION_KEY, description);
        bundle.putBundle(LocationDAO.SERVICES_KEY, getServicesAsBundle());
        bundle.putString(LocationDAO.USER_ID_KEY, user_id);
        bundle.putString(LocationDAO.LOCATION_NAME_KEY, name);
        if (pictures != null) bundle.putStringArrayList(LocationDAO.PICTURES_URI_KEY, (ArrayList<String>) pictures);
        bundle.putBoolean(LocationDAO.CONFIRMED_KEY, confirmed);
        return bundle;
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



    public List<String> getPictures() {
        return pictures;
    }

    public void setPictures(List<String> pictures) {
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

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel out, int i) {
        out.writeBundle(getAsBundle());
    }

    public static final Parcelable.Creator<MapLocation> CREATOR
            = new Parcelable.Creator<MapLocation>() {
        @Override
        public MapLocation createFromParcel(Parcel parcel) {
            return new MapLocation(parcel);
        }

        @Override
        public MapLocation[] newArray(int i) {
            return new MapLocation[i];
        }
    };

    // construit une MapLocation à partir d'une Map générée par fireStore
    public static class Builder {
        private static final String TAG = "LocationBuilder logging";


        public static MapLocation build(Map<String,Object> data) throws NullPointerException {
            double latitude = (double) data.get(LocationDAO.LATITUDE_KEY);
            Log.i(TAG, "build: lat  =" + latitude);
            double longitude = (double) data.get(LocationDAO.LONGITUDE_KEY);
            Log.i(TAG, "build: lng = " + longitude);
            String description = (String) data.get(LocationDAO.DESCRIPTION_KEY);
            Log.i(TAG, "build: description = " + description);
            List<String> pictures = null;
            if(data.containsKey(LocationDAO.PICTURES_URI_KEY)) {
                Log.i(TAG, "mapLocation builder: map contains pictures uri key ");
                List<String> picturesArray = (List<String>) data.get(LocationDAO.PICTURES_URI_KEY);
                if (picturesArray != null){
                    pictures = picturesArray;
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

        // génère un identifiant pour le lieu
        public static String generateId(LatLng point) {

            return String.format(Locale.ENGLISH, Double.toString(point.latitude) + '|' + point.longitude);
        }
    }
}
