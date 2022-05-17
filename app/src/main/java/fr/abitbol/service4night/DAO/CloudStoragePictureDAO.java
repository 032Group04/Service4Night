package fr.abitbol.service4night.DAO;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CloudStoragePictureDAO implements PicturesDAO {
    private final String TAG = "CloudStoragPictureDAO logging";
    private final String PICTURES_ROOT_FOLDER = "pictures";
    private FirebaseStorage storage;
    public static final String USER_ID_METADATA = "user_id";
    private OnCompleteListener<Uri> insertAndSelectListener;
    public CloudStoragePictureDAO(){
        storage = FirebaseStorage.getInstance();
        insertAndSelectListener = null;
    }
    public void registerInsertListener(OnCompleteListener<Uri> listener){
        insertAndSelectListener = listener;
    }


    @Override
    public void insert(String pictureName, String userId, String locationId, Bitmap picture) {
        String refPath = PICTURES_ROOT_FOLDER +"/"+userId+"/"+ pictureName;
        Log.i(TAG, "insert: path : "+refPath);
        StorageReference storageReference = storage.getReference(refPath);

        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata(USER_ID_METADATA,userId)
                .setCustomMetadata(LocationDAO.LOCATION_ID_KEY,locationId)
                .build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        byte[] byteArray = outputStream.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(byteArray);
        if (insertAndSelectListener != null) {
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()){
                        Log.i(TAG, "continueWithTask: successfull");
                        return storageReference.getDownloadUrl();
                    }
                    else{
                        if (task.getException() != null) {
                            Log.i(TAG, "picture task :" + task.getException().getMessage());
                        }
                        else{
                            Log.i(TAG, "then: picture task failed");
                        }

                    }
                    return  null;
                }
            }).addOnCompleteListener(insertAndSelectListener);
        }
        else {
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "continueWithTask: successfull");
                        return storageReference.getDownloadUrl();
                    } else {
                        if (task.getException() != null) {
                            Log.i(TAG, "picture task :" + task.getException().getMessage());
                        } else {
                            Log.i(TAG, "then: picture task failed");
                        }

                    }
                    return null;
                }

            });
        }





    }

    @Override
    public boolean delete(String url) {
        AtomicBoolean success = new AtomicBoolean(false);
        Log.i(TAG, "delete: path : "+url);
        StorageReference storageReference = storage.getReferenceFromUrl(url);
        storageReference.delete().addOnCompleteListener(task -> {
           if (task.isSuccessful()){
               success.set(true);
           }
           else{
               if (task.getException() != null) {
                   Log.i(TAG, "delete task :" + task.getException().getMessage());
               }
               else{
                   Log.i(TAG, "then: delete picture task failed");
               }
           }
        });
        return success.get();
    }

    @Override
    public String select(String pictureName, String userId) {
        String refPath = PICTURES_ROOT_FOLDER +"/"+userId+"/"+ pictureName;
        Log.i(TAG, "select: path : "+refPath);
        StorageReference storageReference = storage.getReference(refPath);
        Task<Uri> selectTask = storageReference.getDownloadUrl();
        if (insertAndSelectListener != null) {
            selectTask.continueWithTask(task -> {
                if (task.isSuccessful()){
                    Log.i(TAG, "select :continueWithTask: successfull");
                    return storageReference.getDownloadUrl();
                }
                else{
                    if (task.getException() != null) {
                        Log.i(TAG, "select task :" + task.getException().getMessage());
                    }
                    else{
                        Log.i(TAG, "then: select task failed");
                    }

                }
                return  null;
            }).addOnCompleteListener(insertAndSelectListener);
        } else {
            Log.i(TAG, "select: can't select without listener");
        }
        return null;
    }

    @Override
    public String update(String url, String pictureName, String userId, String locationId, Bitmap picture) {
        Log.i(TAG, "delete: path : "+url);
        StorageReference storageReference = storage.getReferenceFromUrl(url);

        return null;
    }


}
