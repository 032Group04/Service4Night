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

public class CloudStoragePictureDAO implements PicturesDAO {
    private final String TAG = "CloudStoragPictureDAO logging";
    private final String PICTURES_ROOT_FOLDER = "pictures";
    private FirebaseStorage storage;
    public static final String USER_ID_METADATA = "user_id";
    private OnCompleteListener<Uri> listener;
    public CloudStoragePictureDAO(OnCompleteListener<Uri> _listener){
        storage = FirebaseStorage.getInstance();
        listener = _listener;
    }


    @Override
    public void insert(String name, String userId, String locationId, Bitmap picture) {
        AtomicBoolean success = new AtomicBoolean(false);
        String refPath = PICTURES_ROOT_FOLDER +"/"+userId+"/"+name;
        Log.i(TAG, "insert: path : "+refPath);
        StorageReference storageReference = storage.getReference(refPath);
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setCustomMetadata(LocationDAO.USER_ID_KEY,userId)
                .setCustomMetadata(LocationDAO.LOCATION_ID_KEY,locationId)
                .build();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        picture.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
        byte[] byteArray = outputStream.toByteArray();
        UploadTask uploadTask = storageReference.putBytes(byteArray);
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
        }).addOnCompleteListener(listener);




    }


}
