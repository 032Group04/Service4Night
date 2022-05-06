package fr.abitbol.service4night;

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

public class PicturesUploadTask extends AsyncTask<String,Integer, Boolean> implements OnCompleteListener<Uri> {
    private static final String TAG = "PicturesUploadTask logging";
    Queue<SliderItem> bitmapQueue;
    CloudStoragePictureDAO cloudStorage;
    OnPicturesUploadedListener listener;
    List<Uri> uriList;
    String locationId;
    String userId;



    public PicturesUploadTask(List<SliderItem> items, OnPicturesUploadedListener _listener) {
        super();
        listener = _listener;
        uriList = new ArrayList<>();
        bitmapQueue = new LinkedList<>();
        cloudStorage = new CloudStoragePictureDAO(this);
        bitmapQueue.addAll(items);


    }

    @Override
    protected Boolean doInBackground(String... strings) {

        if (strings.length == 2) {
            Log.i(TAG, "doInBackground called from execute() : thread =" + Thread.currentThread().toString());

            userId = strings[0];
            locationId = strings[1];
        }
        if (strings.length > 0 && strings.length < 3) {
            if (strings[0].equals("continue")) Log.i(TAG, "doInBackground called from within asyncTask : thread =" + Thread.currentThread().toString());

            if (bitmapQueue.peek() != null) {
                Log.i(TAG, "doInBackground: bitmap queue isn't empty");

                SliderItem s = bitmapQueue.remove();
                cloudStorage.insert(s.getName(), userId, locationId, s.getImage());
            } else Log.i(TAG, "doInBackground: bitmap queue is empty");
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
        Log.i(TAG, "onPreExecute called ");

    }

    @Override
    protected void onPostExecute(Boolean result) {
        Log.i(TAG, "onPostExecute called");
        super.onPostExecute(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        Log.i(TAG, "onProgressUpdate called");
        //TODO : créer ProgressBAr avec texte indiquant nombre de photos uploadées
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.i(TAG, "onCancelled called");
        super.onCancelled(result);
    }

    @Override
    public void onComplete(@NonNull Task<Uri> task) {
       if (task.isSuccessful()){
            Log.i(TAG, "onComplete called - task is successful");

            if (task.getResult() != null) {
                Log.i(TAG, "onComplete: picture uploaded, uri = "+task.getResult());
                Log.i(TAG, "onComplete: picture uploaded, uri.path = "+task.getResult().getPath());
                uriList.add(task.getResult());
                if (bitmapQueue.peek() != null) {
                    Log.i(TAG, String.format("onComplete: %d of %d pictures uploaded",uriList.size(), (uriList.size() + bitmapQueue.size() )));
                    doInBackground("continue");
                }
                else{
                    Log.i(TAG, "onComplete in asyncTask : upload done");
                    listener.onPicturesUploaded(uriList);
                }
            } else {
                Log.i(TAG, "onComplete in asyncTask result is null ");
            }
        }

        else Log.i(TAG, "onComplete called - task failed");
    }
}
