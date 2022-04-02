package fr.abitbol.service4night;

import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.io.IOException;

import fr.abitbol.service4night.databinding.FragmentAddLocationBinding;


public class LocationAddFragment extends Fragment implements OnCompleteListener<Void> {

    private FragmentAddLocationBinding binding;

    private final String TAG = "LocationAddFragment logging";
    private Location location;
    private Uri uri;
    private DataBase dataBase;
    private ActivityResultLauncher<Uri> mGetcontent = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            Log.i(TAG, "onActivityResult: picture result : "+result);
            if (result){
                binding.pictureAddLayout.setBackground(null);
                binding.imageView.setImageURI(uri);

                try {
                    location.setPicture(MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri));
                } catch (IOException e) {
                    Toast.makeText(getContext(), "error while getting bitmap from uri", Toast.LENGTH_LONG).show();
                }

            }
        }
    });

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {



        binding = FragmentAddLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dataBase = new DataBase();
        if (getArguments() != null) {
            try {
                LatLng latLng = getArguments().getParcelable("point");
                Log.i(TAG, "onCreateView: intent extras: " + latLng.toString());
                binding.textviewLatitude.setText(Double.toString(latLng.latitude));
                binding.textviewLongitude.setText(Double.toString(latLng.longitude));

                location = new Location(latLng,"");
            }
            catch (Exception e){
                Log.e(TAG,e.getMessage());
            }


        }


        binding.takePictureButton.setOnClickListener(button -> {
            //File file = File.createTempFile(name,".jpg");
//            mGetcontent.launch(location.getUri());
            try {
                takePicture(location.getId()+".jpg");
            } catch (IOException e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG);
            }
        });

        // ajouter possibilité de selectionner une photo dans le téléphone

        binding.buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(LocationAddFragment.this)
//                        .navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                dataBase.registerLocation(location,LocationAddFragment.this);
            }
        });
    }

    public void takePicture(String name) throws IOException {
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + name);
        uri = FileProvider.getUriForFile(getContext(),"fr.abitbol.service4night.fileprovider",file);

        mGetcontent.launch(uri);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: location successfully written. ");
            Toast.makeText(getContext(), getString(R.string.location_add_success), Toast.LENGTH_LONG).show();
        }
        else{
            Log.i(TAG, "onComplete: location failed to be written");
            Toast.makeText(getContext(), getString(R.string.location_add_fail), Toast.LENGTH_LONG).show();
        }
        NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
    }
}