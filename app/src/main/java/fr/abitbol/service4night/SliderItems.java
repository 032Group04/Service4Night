package fr.abitbol.service4night;

import android.graphics.Bitmap;

public class SliderItems {
    //set to String, if you want to add image url from internet
    private Bitmap image;
    SliderItems(Bitmap image) {
        this.image = image;
    }
    public Bitmap getImage() {
        return image;
    }
}