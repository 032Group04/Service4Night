package fr.abitbol.service4night.utils;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.abitbol.service4night.DAO.CloudStoragePictureDAO;
import fr.abitbol.service4night.listeners.OnPictureDeleteListener;

public class PictureDeleteTask extends AsyncTask<String,Integer, Boolean> implements OnPictureDeleteListener {
    private static final String TAG = "PicturesDeleteTask logging";
    Queue<String> urlQueue;
    CloudStoragePictureDAO cloudStorage;
    PicturesDeleteAdapter listener;
    int total;

    int count;



    public PictureDeleteTask(List<String> items, PicturesDeleteAdapter _listener) {
        super();
        total = items.size();
        listener = _listener;
        count = 1;
        urlQueue = new LinkedList<>();
        cloudStorage = new CloudStoragePictureDAO();
        cloudStorage.registerDeleteListener(this);
        urlQueue.addAll(items);


    }

    @Override
    protected Boolean doInBackground(String... strings) {


        if (strings.length > 0 && strings.length < 3) {
            if (strings[0].equals("continue")) Log.i(TAG, "doInBackground called from within asyncTask : thread =" + Thread.currentThread().toString());

            if (urlQueue.peek() != null) {
                Log.i(TAG, "doInBackground: url queue isn't empty");

                String s = urlQueue.remove();

                cloudStorage.delete(s);
            } else Log.i(TAG, "doInBackground: url queue is empty");
            return true;
        }
        else{
            Log.i(TAG, "doInBackground called with wrong arguments count : " + strings.length);
            throw new IllegalArgumentException("UploadAsyncTask : wrong parameters count : " + strings.length);
        }

    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "onPreExecute called thread =" + Thread.currentThread().toString());
        listener.startDeleteBar();

    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i(TAG, "onPostExecute calledthread =" + Thread.currentThread().toString());
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.i(TAG, "onProgressUpdate called");
        if (values[0] == 1) {
            listener.updateDeleteBar(true,values[1]);
        }
        else if (values[0] == 0){
            listener.updateDeleteBar(false,values[1]);
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.i(TAG, "onCancelled called");
        super.onCancelled(result);
    }



    @Override
    public void onPictureDelete(boolean result) {
        count++;

        if (result){
            publishProgress(1,count);
            Log.i(TAG, String.format("onPictureDelete: %d of %d pictures deleted",(total - urlQueue.size()), total));
        }
        else{
            Log.i(TAG, "onPictureDelete called - task failed");
            publishProgress(0,count);
        }
        if (urlQueue.peek() != null) {

            doInBackground("continue");
        }
        else{
            listener.stopDeleteBar();
            Log.i(TAG, "onPictureDelete in asyncTask : delete done");
            listener.onPictureDelete(result);
        }
    }

    /**
     * * testing only
     */
/*public class PicturesUploadTask extends AsyncTask<String,Integer, Boolean>  {
    private static final String TAG = "PicturesUploadTask logging";
    Queue<SliderItem> bitmapQueue;
    //CloudStoragePictureDAO cloudStorage;
    LocationAddFragment listener;
    List<String> uriList;
    String locationId;
    String userId;
    boolean stop;
    int count;
    public PicturesUploadTask(List<SliderItem> items, LocationAddFragment _listener) {
        super();
        stop = false;
        listener = _listener;
        count = 1;
        uriList = new ArrayList<>();
        bitmapQueue = new LinkedList<>();
        //cloudStorage = new CloudStoragePictureDAO(this);
        bitmapQueue.addAll(items);


    }

    @Override
    protected Boolean doInBackground(String... strings) {
        synchronized (this) {
            try {
                Log.i(TAG, "TESTING doInBackground sleeping: ");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (strings.length == 2) {

                userId = strings[0];
                locationId = strings[1];
            }
            if (strings.length > 0 && strings.length < 3) {

                if (bitmapQueue.peek() != null) {
                    Log.i(TAG, "TESTING :doInBackground: bitmap queue isn't empty");

                    SliderItem s = bitmapQueue.remove();


                } else Log.i(TAG, "TESTING :doInBackground: bitmap queue is empty");
                if (strings[0].equals("fail")) {
                    onComplete(false, false);
                } else if (strings[0].equals("stop")) {

                    onComplete(true, true);
                }
                else {
                    Log.i(TAG, "TESTING :doInBackground called from execute() : thread =" + Thread.currentThread().toString());

                    onComplete(true,false);
                }


                return true;
            } else {
                Log.i(TAG, "TESTING :doInBackground called with wrong arguments count : " + strings.length);
                throw new IllegalArgumentException("UploadAsyncTask : wrong parameters count : " + strings.length);
            }
        }
    }



    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "onPreExecute called thread =" + Thread.currentThread().toString());
        listener.startProgressBar();

    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i(TAG, "onPostExecute calledthread =" + Thread.currentThread().toString());
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.i(TAG, "onProgressUpdate called");
        if (values[0] == 1) {
            listener.updateProgressBar(true,values[1]);
        }
        else if (values[0] == 0){
            listener.updateProgressBar(false,values[1]);
        }
        //TODO : créer ProgressBAr avec texte indiquant nombre de photos uploadées
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.i(TAG, "onCancelled called");
        super.onCancelled(result);
    }


    public void onComplete(boolean task, boolean finished) {
        count++;
        if (task){
            Log.i(TAG, "TESTING :onComplete called - task is successful");
                if (finished){
                    uriList.add("http://www.lastPicture.com");
                    listener.onPicturesUploaded(uriList);
                }
                else {
                    if (bitmapQueue.peek() != null) {
                        Log.i(TAG, String.format("TESTING :testing: onComplete "));
                        uriList.add("http://www.firstPicture.com");

                        publishProgress(1,count);
                        doInBackground("fail");
                    }
                }
        }
        else {
            Log.i(TAG, "TESTING :onComplete: task failed");

            publishProgress(0,count);
            doInBackground("stop");

        }

    }*/
}
