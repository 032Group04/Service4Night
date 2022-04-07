package fr.abitbol.service4night;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import fr.abitbol.service4night.services.Service;

public class Location {

    private String description;
    private String id;
    private LatLng point;
    private Bitmap picture;
    private Map<String, Service> services;


    public Location(double latitude, double longitude, String _description,HashMap<String,Service> _services)  {
        point = new LatLng(latitude, longitude);
        services = _services;
        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(latitude)+'|'+ longitude);


    }

    public Location(LatLng _point, String _description, HashMap<String,Service> _services) {
        point = _point;
        services = _services;

        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(point.latitude)+'|'+ point.longitude);


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
    public Service gotService(Service service){
        return services.getOrDefault(service.getLabel(),null);
    }
    public Service gotService(String label){
        return services.getOrDefault(label,null);
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj){
            return true;
        }
        if (!(obj instanceof Location)){
            return false;
        }
        else{
            return id.equals(((Location) obj).getId());
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

    public Bitmap getPicture() {
        return picture;
    }

    public void setPicture(Bitmap bitmap) {

        this.picture = bitmap;
    }

    public boolean isLocationInArea (LatLngBounds bounds){
          return bounds.contains(point);
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

    public LatLng getPoint() {
        return point;
    }
}
