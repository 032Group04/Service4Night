package fr.abitbol.service4night.pictures;

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
    PicturesUploader listener;
    List<String> uriList;
    String locationId;
    String userId;
    int count;



    public PicturesUploadTask(List<SliderItem> items, PicturesUploader _listener) {
        super();

        listener = _listener;
        count = 1;
        uriList = new ArrayList<>();
        bitmapQueue = new LinkedList<>();
        cloudStorage = new CloudStoragePictureDAO();
        cloudStorage.registerInsertListener(this);
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
    }

    @Override
    protected void onCancelled(Boolean result) {
        Log.i(TAG, "onCancelled called");
        super.onCancelled(result);
    }

    @Override
    public void onComplete(@NonNull Task<Uri> task) {
        count++;
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete called - task is successful");

            if (task.getResult() != null) {
                Log.i(TAG, "onComplete: picture uploaded, uri = "+task.getResult());
                Log.i(TAG, "onComplete: picture uploaded, uri.path = "+task.getResult().getPath());
                uriList.add(task.getResult().toString());
                if (bitmapQueue.peek() != null) {
                    publishProgress(1,count);
                    Log.i(TAG, String.format("onComplete: %d of %d pictures uploaded",uriList.size(), (uriList.size() + bitmapQueue.size() )));
                    doInBackground("continue");
                }
                else{
                    listener.stopProgressBar();
                    Log.i(TAG, "onComplete in asyncTask : upload done");
                    listener.onPicturesUploaded(uriList);
                }
            } else {
                publishProgress(0,count);
                Log.i(TAG, "onComplete in asyncTask result is null ");
            }
        }

        else{
            Log.i(TAG, "onComplete called - task failed");

            if (bitmapQueue.peek() != null) {
                publishProgress(0,count);
                doInBackground("continue");
            }
            else{
                listener.stopProgressBar();
                Log.i(TAG, "onComplete in asyncTask : upload done");
                listener.onPicturesUploaded(uriList);
            }
        }
    }


}
