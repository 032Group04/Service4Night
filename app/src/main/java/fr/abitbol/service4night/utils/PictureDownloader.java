package fr.abitbol.service4night.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.listeners.OnPictureDownloadListener;

public class PictureDownloader {
    private static final String TAG = "PictureDownloader logging";
    public static String PICTURES_LOCAL_FOLDER = "Locations pictures";
    public static String DELETED_PICTURES_VALUE = "deleted";

    public static synchronized void getBitmapsFromURL(MapLocation mapLocation, ArrayList<String> picturesNames,
                    ArrayList<String> picturesPaths, File externalFilesDir,OnPictureDownloadListener listener) {

        new Thread(() -> {
            try {
                int i = 1;
                ArrayList<SliderItem> images  = new ArrayList<>();
                for (String s : mapLocation.getPictures()) {

                    if (s != null) {

                        if (s.equals(DELETED_PICTURES_VALUE)) {

                        } else {
                            String pictureName;
                            String picTurePath;
                            URL url = new URL(s);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();

                            InputStream input = connection.getInputStream();

                            File mediaStorageDir = new File(externalFilesDir, PictureDownloader.PICTURES_LOCAL_FOLDER);

                            // Create the storage directory if it does not exist
                            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                                Log.d(TAG, "failed to create directory");
                                listener.onPictureDownload(null);
                            }
                            Log.i(TAG, "takePicture: file path is: " + mediaStorageDir.getPath());
                            // Return the file target for the photo based on filename
                            pictureName = MapLocation.generatePictureName(mapLocation.getId(), i);
                            picTurePath = mediaStorageDir.getPath() + File.separator + pictureName;
                            File file = new File(picTurePath);
                            if (!file.exists()) {
                                Log.i(TAG, "getBitmapsFromURL: picture file does not exist");
                                file.createNewFile();
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
                                FileOutputStream fos = new FileOutputStream(file);
                                int in = 0;
                                Log.i(TAG, "getBitmapsFromURL: writing on file...");
                                while ((in = bufferedInputStream.read()) != -1) {
                                    fos.write(in);
                                }

                            } else {
                                Log.i(TAG, "getBitmapsFromURL: picture already exists");
                            }
                            picturesPaths.add(picTurePath);
                            picturesNames.add(pictureName);

                            Log.i(TAG, "getBitmapsFromURL: getting Bitmap from file");
                            Bitmap myBitmap = BitmapFactory.decodeFile(picTurePath);
                            myBitmap = ExifUtil.rotateBitmap(picTurePath, myBitmap);
                            Log.i(TAG, "getBitmapsFromURL: bitmap width : " + myBitmap.getWidth() + " height : " + myBitmap.getHeight());
                            images.add(new SliderItem(myBitmap, pictureName));

                        }
                        i++;
                    }

                }
                listener.onPictureDownload(images);



            } catch (IOException e) {


            }

        }).start();

    }


}
