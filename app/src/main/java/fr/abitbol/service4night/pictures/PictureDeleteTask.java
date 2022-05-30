/*
 * Nom de classe : PictureDeleteTask
 *
 * Description   : suppression asynchrone de plusieurs images
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.pictures;

import android.os.AsyncTask;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.abitbol.service4night.DAO.CloudStoragePictureDAO;
import fr.abitbol.service4night.listeners.OnPictureDeleteListener;

public class PictureDeleteTask extends AsyncTask<String,Integer, Boolean> implements OnPictureDeleteListener {
    private static final String TAG = "PicturesDeleteTask logging";
    Queue<String> urlQueue;
    CloudStoragePictureDAO cloudStorage;
    PicturesDeleter listener;
    int total;

    int count;



    public PictureDeleteTask(List<String> items, PicturesDeleter _listener) {
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


}
