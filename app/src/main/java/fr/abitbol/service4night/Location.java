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
    private String name;
    private LatLng point;
    private Bitmap picture;
    private Map<String, Service> services;
    private String user_id;
    private boolean confirmed;


    public Location(double latitude, double longitude, String _description,HashMap<String,Service> _services, String _user_id,String _name,boolean _confirmed)  {
        point = new LatLng(latitude, longitude);
        services = _services;
        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(latitude)+'|'+ longitude);
        user_id = _user_id;
        name = _name;
        confirmed = _confirmed;

    }

    public Location(LatLng _point, String _description, HashMap<String,Service> _services,String _user_id,String _name,boolean _confirmed) {
        point = _point;
        services = _services;
        user_id = _user_id;
        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(point.latitude)+'|'+ point.longitude);
        name = _name;
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
}
