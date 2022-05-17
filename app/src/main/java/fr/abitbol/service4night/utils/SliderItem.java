package fr.abitbol.service4night.utils;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import fr.abitbol.service4night.MapLocation;

public class SliderItem implements Parcelable {
    private static final String TAG = "SliderItem logging";
    private static final String IMAGE_PARCEL_NAME = "image";
    private static final String NAME_PARCEL_NAME = "name";
    private static final String PATH_PARCEL_NAME = "path";
    //set to String, if you want to add image url from internet
    private Bitmap image;
    private String name;
    private String path;
    public SliderItem(Bitmap _image, String _name) {
        Log.i(TAG, "SliderItem: new image : " +_name);
        image = _image;
        name = _name;
        path = null;
    }
    public SliderItem(Bitmap _image, String _name,String _path){
        Log.i(TAG, "SliderItem: new image : " +_name +" - path : "+_path);
        image = _image;
        name = _name;
        path = _path;
    }
    public SliderItem (Parcel parcel){
        Bundle bundle = parcel.readBundle(getClass().getClassLoader());
        image = (Bitmap) bundle.getParcelable(IMAGE_PARCEL_NAME);
        name = bundle.getString(NAME_PARCEL_NAME);
        if (bundle.containsKey(PATH_PARCEL_NAME)){
            path = bundle.getString(PATH_PARCEL_NAME);
        }
        else {path = null;}

    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Bitmap getImage() {
        return image;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(IMAGE_PARCEL_NAME,image);
        bundle.putString(NAME_PARCEL_NAME,name);
        if (path != null) {
            bundle.putString(PATH_PARCEL_NAME, path);
        }
        parcel.writeBundle(bundle);
    }
    public static final Parcelable.Creator<SliderItem> CREATOR
            = new Parcelable.Creator<SliderItem>() {
        @Override
        public SliderItem createFromParcel(Parcel parcel) {
            return new SliderItem(parcel);
        }

        @Override
        public SliderItem[] newArray(int i) {
            return new SliderItem[i];
        }
    };
}