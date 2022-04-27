package fr.abitbol.service4night;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.abitbol.service4night.databinding.FragmentAddLocationBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationAddFragment extends Fragment implements OnCompleteListener<Void> {

    private FragmentAddLocationBinding binding;

    private final String TAG = "LocationAddFragment logging";
    private LatLng point;
    private String description;
    private String name;
    private Map<String, Service> services;
    private Bitmap picture;
    List<SliderItems> images;
    private Uri uri;
    private DataBase dataBase;
    private ViewPager2 viewPager;
    //TODO : demander au prof meilleure méthode (re-récupérer user ou passer id en argument)
    private FirebaseUser user;
    private ActivityResultLauncher<Uri> mGetcontent = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {



        @Override
        public void onActivityResult(Boolean result) {
            Log.i(TAG, "onActivityResult: picture result : "+result);
            if (result){
                //binding.pictureAddLayout.setBackground(null);
                //binding.imageView.setImageURI(uri);
                images.remove(0);

                try {
                    picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),uri);
                    images.add(new SliderItems(picture));
                    viewPager.setAdapter(new SliderAdapter(images,viewPager));

                } catch (IOException e) {
                    Toast.makeText(getContext(), "error while getting bitmap from uri", Toast.LENGTH_LONG).show();
                }

            }
        }
    });

    @Override
    public void onResume() {
        Log.i(TAG, "onResume called ");
        super.onResume();
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            if (!(NavHostFragment.findNavController(LocationAddFragment.this).popBackStack())){
                Toast.makeText(getContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                
            }
        }



        images = new ArrayList<>();
        binding = FragmentAddLocationBinding.inflate(inflater, container, false);
        if (binding.addWaterCheckBox.isChecked()){
            binding.addDrinkableCheckBox.setEnabled(true);
        }
        else{
            binding.addDrinkableCheckBox.setEnabled(false);
        }
        if (binding.addPrivateNetworkRadioButton.isChecked()){
            binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
            binding.internetPriceEditText.setVisibility(View.VISIBLE);
        }
        else{
            binding.addInternetPriceLabel.setVisibility(View.GONE);
            binding.internetPriceEditText.setVisibility(View.GONE);
        }
        if (binding.addDrainageCheckbox.isChecked()){
            binding.addDarkWaterCheckBox.setVisibility(View.VISIBLE);
        }
        else{
            binding.addDarkWaterCheckBox.setVisibility(View.GONE);
        }
        binding.addWaterCheckBox.setOnClickListener(view -> {
            if (((CheckBox) view).isChecked()){
                binding.addDrinkableCheckBox.setEnabled(true);
            }
            else{
                binding.addDrinkableCheckBox.setEnabled(false);
            }
        });

        binding.addInternetTypeRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            Log.i(TAG, "RadioGroup onCheckedChangeListener called; i = " + i);
            if (binding.addPublicWifiRadioButton.isChecked()){
                binding.addInternetPriceLabel.setVisibility(View.GONE);
                binding.internetPriceEditText.setVisibility(View.GONE);
            }
            else{
                binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
                binding.internetPriceEditText.setVisibility(View.VISIBLE);
            }
        });
        binding.addDrainageCheckbox.setOnClickListener(view -> {
            if (binding.addDrainageCheckbox.isChecked()){
                binding.addDarkWaterCheckBox.setVisibility(View.VISIBLE);
            }
            else{
                binding.addDarkWaterCheckBox.setVisibility(View.GONE);
            }
        });

        viewPager = binding.locationAddViewPager;
//        images.add(0,new SliderItems(R.drawable.add_picture));

//        List<SliderItems> sliderItems = new ArrayList<>();
//        images.add(new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable._0210621_071025)));
//        images.add(new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable._0210708_160334)));

        images.add(0,new SliderItems(BitmapFactory.decodeResource(getResources(),R.drawable.test_)));


//        viewPager.setAdapter(new SliderAdapter(sliderItems,viewPager));

//        images.add(BitmapFactory.decodeResource(getResources(),R.drawable.ic_search));
        viewPager.setAdapter(new SliderAdapter(images,viewPager));

        if (savedInstanceState == null) {

            Log.i(TAG, "onViewCreated: images size : " + images.size());
            dataBase = new DataBase();
            if (getArguments() != null) {
                try {
                    point = ((LatLng)getArguments().getParcelable("point"));
                    Log.i(TAG, "onCreateView: intent extras: " + point.toString());
                    binding.locationAddTextviewLatitude.setText(Double.toString(point.latitude));
                    binding.locationAddTextviewLongitude.setText(Double.toString(point.longitude));
                    //TODO : ajouter locale au geocoder selon position utilisateur
                    Geocoder geocoder = new Geocoder(getContext());
                    List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
                    Address address = addresses.get(0);
//                    for (Address address : addresses) {
//                        Log.i(TAG, "onViewCreated: address lines : " + address.getMaxAddressLineIndex());
//                        Log.i(TAG, "onViewCreated: adress to string: " + address.toString());
//                        Log.i(TAG, "onViewCreated: address url : " + address.getUrl());
//                        Log.i(TAG, "onViewCreated: adress");
//
//                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                            Log.i(TAG, "onViewCreated: adresss Line " + i + " : " + address.getAddressLine(i));
//
//                        }
//                    }
                    Log.i(TAG, "onViewCreated: address 0 = " + address.getAddressLine(0));
                    if (address.getFeatureName() != null && address.getFeatureName().length() > 8){
                        name = address.getFeatureName();
                        Log.i(TAG, "onViewCreated: adress feature name :" + name);
                    }
                    else{
                        name = address.getAddressLine(0);
                    }
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null){
                        Log.i(TAG, "onViewCreated: main activity not null");
                        ActionBar actionBar = mainActivity.getSupportActionBar();
                        if (actionBar != null){
                            Log.i(TAG, "onViewCreated: action bar not null");
                            actionBar.setTitle(name);
                        }
                        else Log.i(TAG, "onViewCreated: action bar is null");
                    }
                    else Log.i(TAG, "onViewCreated: mainActivity is null");


                }
                catch (Exception e){
                    Log.e(TAG,e.getMessage());
                    Toast.makeText(getContext(), getString(R.string.arguments_missing_error), Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(LocationAddFragment.this).popBackStack();
                }


            }
            else{
                point = new LatLng(0,0);
                // récupérer coordonnées utilisateur
            }
        }

        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);

//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        binding.takePictureButton.setOnClickListener(button -> {
            //File file = File.createTempFile(name,".jpg");
//            mGetcontent.launch(location.getUri());
            try {
                takePicture(Location.Builder.generateId(point));
            } catch (IOException e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG);
            }
        });

        //TODO : ajouter possibilité de selectionner une photo dans le téléphone

        binding.buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(LocationAddFragment.this)
//                        .navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                services = new HashMap<>();


                if (processInputs()) {
                    dataBase.registerLocation(new Location(point, description, services, user.getUid(),name,false), LocationAddFragment.this);
                }
            }
        });
    }
    public boolean processInputs(){
        if (binding.addDescriptionEditText.getText().length() < 4){
            Toast.makeText(getContext(), getString(R.string.empty_description), Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            description = binding.addDescriptionEditText.getText().toString();
            Log.i(TAG, "onClick: description = "+ description);
        }
        boolean service = false;


        if (binding.addWaterCheckBox.isChecked()){
            service = true;
            boolean drinkable = binding.addDrinkableCheckBox.isChecked();
            boolean update = services.containsKey(Service.WATER_SERVICE);
            Log.i(TAG, "processServices: water checked : drikable = "+ drinkable+" , update = " + update);
            double price;
            if(binding.addWaterPriceEditText.getText().length() == 0){
                Log.i(TAG, "getServices: water price is empty");
                price = 0;
            }
            else{
                try{
                    price = parsePrice(binding.addWaterPriceEditText,"water service");

                }catch (NumberFormatException e){
                    Log.i(TAG, "processInputs: error while parsing price");
                    Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            if (update){
                services.replace(Service.WATER_SERVICE,new WaterService(price,drinkable));
            }
            else{
                services.put(Service.WATER_SERVICE,new WaterService(price,drinkable));
            }

        }
        if (binding.addElectricityCheckBox.isChecked()){
            service = true;
            boolean update = services.containsKey(Service.ELECTRICITY_SERVICE);
            Log.i(TAG, "processServices: electricity checked : update = " + update);

            double price;
            if(binding.electricityPriceEditText.getText().length() == 0){
                Log.i(TAG, "getServices: water price is empty");
                price = 0;
            }
            else{
                try{
                    price = parsePrice(binding.electricityPriceEditText,"electricity service");

                }catch (NumberFormatException e){
                    return false;
                }
            }
            if (update){
                services.replace(Service.ELECTRICITY_SERVICE,new ElectricityService(price));
            }
            else{
                services.put(Service.ELECTRICITY_SERVICE,new ElectricityService(price));
            }

        }
        if (binding.addInternetCheckBox.isChecked()){
            service = true;
            boolean update = services.containsKey(Service.INTERNET_SERVICE);
            InternetService.ConnectionType connectionType;

            double price = 0;
            if (binding.addPrivateNetworkRadioButton.isChecked()){
                 connectionType = InternetService.ConnectionType.private_provider;
                if(binding.internetPriceEditText.getText().length() > 0) {
                    try {
                        price = parsePrice(binding.internetPriceEditText,"internet service");

                    } catch (NumberFormatException e) {
                        Log.i(TAG, "processInputs: " + e.getMessage());
                        return false;
                    }
                }
            }
            else{
                connectionType = InternetService.ConnectionType.public_wifi;
            }
            Log.i(TAG, "processServices: internet checked : connection type = "+ connectionType.name()+" update = " + update);
            if (update){
                if (connectionType.equals(InternetService.ConnectionType.public_wifi)) {
                    services.replace(Service.INTERNET_SERVICE, new InternetService(connectionType));
                }
                else{
                    services.replace(Service.INTERNET_SERVICE,new InternetService(connectionType,price));
                }
            }
            else{
                if (connectionType.equals(InternetService.ConnectionType.public_wifi)) {
                    services.put(Service.INTERNET_SERVICE, new InternetService(connectionType));
                }
                else{
                    services.put(Service.INTERNET_SERVICE,new InternetService(connectionType,price));
                }
            }

        }
        if (binding.addDumpsterCheckBox.isChecked()){
            service = true;
            boolean update = services.containsKey(Service.DUMPSTER_SERVICE);
            Log.i(TAG, "processInputs: dumpster service checked , update = " + update);
            if (update){
                services.replace(Service.DUMPSTER_SERVICE,new DumpService());
            }
            else{
                services.put(Service.DUMPSTER_SERVICE,new DumpService());
            }
        }
        if (binding.addDrainageCheckbox.isChecked()){
            service = true;
            boolean update = services.containsKey(Service.DRAINAGE_SERVICE);
            boolean blackWater = binding.addDarkWaterCheckBox.isChecked();
            Log.i(TAG, "processInputs: drainage service checked , update = " + update + " , blackwater = " + blackWater);

            if (update){
                services.replace(Service.DRAINAGE_SERVICE,new DrainService(blackWater));
            }
            else{
                services.put(Service.DRAINAGE_SERVICE,new DrainService(blackWater));
            }
        }
        //réfléchir a remplir services au coup par coup avec des listener
        if (service) {
            return true;
        }
        else{
            Toast.makeText(getContext(), getString(R.string.no_service_selected), Toast.LENGTH_LONG).show();
            return false;
        }

    }
    public double parsePrice(EditText editText,String name) throws NumberFormatException{
        double price;
        try{
            price = Float.parseFloat(editText.getText().toString());
        }catch (NumberFormatException e){
            Log.i(TAG, "processServices: " + e.getMessage()+" from " + editText.getTransitionName());
            Toast.makeText(getContext(), getString(R.string.price_parsing_error_named)+name, Toast.LENGTH_SHORT).show();
            editText.requestFocus();
            throw e;

        }
        return price;
    }

    public void takePicture(String name) throws IOException {
        File mediaStorageDir = new File(getContext().getFilesDir(), "Service4night pics");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        Log.i(TAG, "takePicture: file path is: "+ mediaStorageDir.getPath());
        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + name);
        uri = FileProvider.getUriForFile(getContext(),"fr.abitbol.service4night.fileprovider",file);

        mGetcontent.launch(uri);
    }
    public void resume(Location location){
        //TODO : afficher les données de location pour récupérer ajout abandonné

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
        binding = null;
    }

    @Override
    public void onComplete(@NonNull Task task) {
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: location successfully written. ");
            Toast.makeText(getContext(), getString(R.string.location_add_success), Toast.LENGTH_SHORT).show();
        }
        else{
            Log.i(TAG, "onComplete: location failed to be written");
            Log.i(TAG, "onComplete: task to string : " + task.toString());
            Log.i(TAG, "onComplete: task get Exception : "+ task.getException());
            Toast.makeText(getContext(), getString(R.string.location_add_fail), Toast.LENGTH_SHORT).show();
        }
        NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
    }
}