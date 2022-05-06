package fr.abitbol.service4night;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class ExifUtil {
    private static final String TAG = "ExifUtil logging";

    /**
     *  http://sylvana.net/jpegcrop/exif_orientation.html
     */
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
    
    private static int getExifOrientation(String src) throws IOException {
        int orientation = 1;
        
        try {
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            //src.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
//            byte[] bitMapBytes = outputStream.toByteArray();

            ExifInterface exif = new ExifInterface(src);


            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.i(TAG, "getExifOrientation: orientation = "+orientation);
//            Constructor<?> exifConstructor = exifClass.getConstructor(new Class[] { String.class });
//            Object exifInstance = exifConstructor.newInstance(new Object[] { src });
//            Method getAttributeInt = exifClass.getMethod("getAttributeInt", new Class[] { String.class, int.class });
//            Field tagOrientationField = exifClass.getField("TAG_ORIENTATION");
//            String tagOrientation = (String) tagOrientationField.get(null);
//            orientation = (Integer) getAttributeInt.invoke(exifInstance, new Object[] { tagOrientation, 1});
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        return orientation;
    }
}
