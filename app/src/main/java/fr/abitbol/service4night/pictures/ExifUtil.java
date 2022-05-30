/*
 * Nom de classe : ExifUtil
 *
 * Description   : gère l'orientation et le redimensionnement des images
 *
 * Auteur        : Olivier Baylac, modification du code de Taro Kobayashi (https://gist.github.com/9re)
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.pictures;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;

import android.util.Log;

public class ExifUtil {
    private static final String TAG = "ExifUtil logging";

    /**
     *  http://sylvana.net/jpegcrop/exif_orientation.html
     */

    public static Bitmap resizeBitmap(Bitmap bitmap){
        //TODO si bloquage orientation photos : a modifier selon blocage orientation photos
        Log.i(TAG, "resizeBitmap: untouched bytes = "+ bitmap.getByteCount());
        if (bitmap.getHeight() > bitmap.getWidth()){
            Log.i(TAG, "resizeBitmap: height is bigger");
        }
        else{ Log.i(TAG, "resizeBitmap: width is bigger");}
        
        if (bitmap.getWidth() > 800 ){
            Log.i(TAG, "resizeBitmap: width > 800");
            Matrix matrix = new Matrix();
            float scale= (float) 800 /(float)bitmap.getWidth();

            matrix.setScale(scale,scale);
            Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            Log.i(TAG, "resizeBitmap: new width = "+resized.getWidth()+" new height = "+resized.getHeight()+ "byte count = "+resized.getByteCount());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            return resized;
        }
        else return bitmap;
    }
    public static Bitmap rotateBitmap(String src, Bitmap bitmap) {
        try {
            int orientation = getExifOrientation(src);
            
            if (orientation == 1) {
                return bitmap;
            }
            
            Matrix matrix = new Matrix();
            switch (orientation) {
            case 2:
                Log.i(TAG, "rotateBitmap: orientation is FLIP HORIZONTAL");
                matrix.setScale(-1, 1);
                break;
            case 3:
                Log.i(TAG, "rotateBitmap: orientation is ROTATE 180");
                matrix.setRotate(180);
                break;
            case 4:
                Log.i(TAG, "rotateBitmap: orientation is FLIP VERTICAL");
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case 5:
                Log.i(TAG, "rotateBitmap: orientation is TRANSPOSE");
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 6:
                Log.i(TAG, "rotateBitmap: orientation is ROTATE 90");
                matrix.setRotate(90);
                float scaleX= (float) bitmap.getWidth() /(float)bitmap.getHeight();
                float scaleY = (float) bitmap.getWidth()/(float)bitmap.getHeight();
                Log.i(TAG, "rotateBitmap: scaleX = "+scaleX+" scaleY = "+scaleY);
                Log.i(TAG, "rotateBitmap: new dimension expected: x = " + (bitmap.getWidth()*scaleX)+" y = "+(bitmap.getHeight()*scaleY));
                matrix.postScale( scaleX,scaleY);
                break;
            case 7:
                Log.i(TAG, "rotateBitmap: orientation is TRANSVERSE");
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case 8:
                Log.i(TAG, "rotateBitmap: orientation is ROTATE 270");
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
            }
            
            try {
                Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();

                Log.i(TAG, "rotateBitmap: new dimension returned : x = "+oriented.getWidth()+" y = "+oriented.getHeight());
                return oriented;
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        return bitmap;
    }
    
    public static int getExifOrientation(String src) throws IOException {
        int orientation = 1;
        
        try {
            ExifInterface exif = new ExifInterface(src);
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.i(TAG, "getExifOrientation: orientation = "+orientation);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return orientation;
    }
}
