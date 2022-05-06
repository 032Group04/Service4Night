package fr.abitbol.service4night;

import android.graphics.Bitmap;
import android.util.Log;

public class SliderItem {
    private static final String TAG = "SliderItem logging";
    //set to String, if you want to add image url from internet
    private Bitmap image;
    private String name;
    SliderItem(Bitmap _image, String _name) {
        Log.i(TAG, "SliderItem: new image : " +_name);
        image = _image;
        name = _name;
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
}