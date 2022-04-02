package fr.abitbol.service4night;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Location {

    private String description;
    private String id;
    private LatLng point;
    private Bitmap picture;
    private boolean electricity, water, drinkableWater, wifi,dumpster,drainage;
    public Location(double latitude, double longitude, String _description)  {
        point = new LatLng(latitude, longitude);

        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(latitude)+'|'+ longitude);


    }
    public Location(LatLng _point, String _description) {
        point = _point;

        description = _description;
        id = String.format(Locale.FRANCE,Double.toString(point.latitude)+'|'+ point.longitude);


    }

    public Map<String,Object> getMappedLocation(){
        Map<String,Object> locationMap = new HashMap<>();
        locationMap.put(id,this);
        return locationMap;

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
