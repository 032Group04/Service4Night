package fr.abitbol.service4night.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import fr.abitbol.service4night.DAO.DAOFactory;
import fr.abitbol.service4night.DAO.LocationDAO;
import fr.abitbol.service4night.utils.ExifUtil;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.utils.PictureDownloader;
import fr.abitbol.service4night.utils.PicturesUploadAdapter;
import fr.abitbol.service4night.utils.PicturesUploadTask;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.utils.SliderAdapter;
import fr.abitbol.service4night.utils.SliderItem;
import fr.abitbol.service4night.utils.UserLocalisation;
import fr.abitbol.service4night.databinding.FragmentAddLocationBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationAddFragment extends Fragment implements OnCompleteListener<Void>, PicturesUploadAdapter {

    private FragmentAddLocationBinding binding;
    //TODO si temps créer interface contenant méthodes et constantes communes LocationAdd et LocationUpdate
    private final String TAG = "LocationAddFragment logging";
    private static final String PICTURE_TAKEN_NAME = "pictureTaken";
    private static final String PICTURES_PATHS_LIST_NAME = "picturesPaths";
    private static final String MAPLOCATION_NAME = "mapLocation";
    private static final String POINT_NAME = "point";
    private static final String PICTURES_NAMES_LIST_NAME = "picturesNames";
    private static final String EMPTY_VIEWPAGER_PICTURE_NAME = "add_picture.png";
    private static final String CURRENT_PICTURE_NAME = "currentPictureName";
    private static final String CURRENT_PICTURE_PATH = "currentPicturePath";
    private static final String CURRENT_PICTURE_URI = "currentPictureUri";
    private LatLng point;
    private String name;
    private MapLocation mapLocation;
    ArrayList<SliderItem> images;
    private Uri currentPictureUri;
    private LocationDAO dataBase;
    private ViewPager2 viewPager;
    private FirebaseUser user;
    private boolean pictureTaken;
    private ArrayList<String> picturesPaths;
    private ArrayList<String> picturesNames;
    private String currentPicTurePath;
    private String currentPictureName;
    private String locationId;
    private List<String> picturesCloudUris;
    private boolean[] freePictureSpace;

    //TODO: problème écran noir depuis mapActivity quand réseau faible
    private ViewPager2.OnPageChangeCallback onPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            Log.i(TAG, "onPageSelected: position = "+ position);
            Log.i(TAG, "onPageSelected: viewpager item path = "+((SliderAdapter)viewPager.getAdapter()).getSliderItem(position).getPath());
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
            Log.i(TAG, "onPageScrollStateChanged: state = "+ (((state ==ViewPager2.SCROLL_STATE_SETTLING)? "settling":"dragging : " + state)));
        }
    };
    private ActivityResultLauncher<Uri> mGetcontent = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {



        @Override
        public void onActivityResult(Boolean result) {
            Log.i(TAG, "onActivityResult: picture result : "+result);
            if (result){
                //binding.pictureAddLayout.setBackground(null);
                //binding.imageView.setImageURI(uri);
                if(!pictureTaken){
                    pictureTaken = true;

                    if (images.get(0).getName().equals(EMPTY_VIEWPAGER_PICTURE_NAME)) {
                        images.remove(0);
                    }

                    viewPager.registerOnPageChangeCallback(onPageChangeCallback);
                }

                try {
                    Bitmap picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentPictureUri);

                    File file = new File(currentPicTurePath);
                    Log.i(TAG, "onActivityResult: file exists :" + file.exists());
                    picture = ExifUtil.rotateBitmap(currentPicTurePath,picture);
                    picture = ExifUtil.resizeBitmap(picture);
                    int orientation = ExifUtil.getExifOrientation(currentPicTurePath);
                    images.add(new SliderItem(picture,currentPictureName, currentPicTurePath));
                    picturesPaths.add(currentPicTurePath);
                    picturesNames.add(currentPictureName);


                    showPictures();
                    freePictureSpace[(getPicturesCount()-1)] = false;
                    currentPicTurePath = null;
                    currentPictureName = null;
                    currentPictureUri = null;
                    enablePictureDelete(true);



                } catch (IOException e) {
                    Toast.makeText(getContext(), "error while getting bitmap from uri", Toast.LENGTH_LONG).show();
                    currentPicTurePath = null;
                    currentPictureUri = null;
                    currentPictureName = null;
                }

            }
        }
    });
    private ActivityResultLauncher<String> pickPictureContract = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            if (result != null) {
                try {
                    Bitmap picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), result);
                    if(!pictureTaken){
                        pictureTaken = true;
    
                        if (images.get(0).getName().equals(EMPTY_VIEWPAGER_PICTURE_NAME)) {
                            images.remove(0);
                        }
    
                        viewPager.registerOnPageChangeCallback(onPageChangeCallback);
                    }
                    currentPicTurePath =result.toString();
    
                    Log.i(TAG, "onActivityResult: result to string = "+ result.toString());
                    File file = new File(currentPicTurePath);
                    if (!file.exists()){
                        Log.i(TAG, "onActivityResult pick: uri path file doesn't exist");
    
    
                    }
                    else{
                        Log.i(TAG, "onActivityResult pick: file from uri.toString exist");
                        picture = ExifUtil.rotateBitmap(currentPicTurePath,picture);
                    }
                    Log.i(TAG, "onActivityResult pick: file exists :" + file.exists());
    
                    picture = ExifUtil.resizeBitmap(picture);
                    //TODO:si temps  accepter uniquement formaat paysage
                    int pictureNum = getPicturesCount();
                    currentPictureName = MapLocation.generatePictureName(mapLocation.getId(), pictureNum);
                    images.add(new SliderItem(picture,currentPictureName, currentPicTurePath));
                    showPictures();
                    freePictureSpace[(pictureNum-1)] = false;
                    currentPicTurePath = null;
                    currentPictureUri = null;
                    currentPictureName = null;
                    enablePictureDelete(true);
                } catch (IOException e) {
                    Log.i(TAG, "onActivityResult: erreur : "+ e.getMessage());
                }
            }
            else{
                currentPicTurePath = null;
                currentPictureUri = null;
                currentPictureName = null;
                Log.i(TAG, "onActivityResult: uri result is null");
            }
        }
    });



    @Override
    public void onResume() {
        Log.i(TAG, "onResume called ");
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(PICTURE_TAKEN_NAME,pictureTaken);
        if (currentPictureName != null){
            outState.putString(CURRENT_PICTURE_NAME,currentPictureName);
        }
        if (currentPicTurePath != null){
            outState.putString(CURRENT_PICTURE_PATH,currentPicTurePath);
        }
        if (currentPictureUri != null){
            outState.putParcelable(CURRENT_PICTURE_URI, currentPictureUri);
        }
        if (picturesPaths != null && !picturesPaths.isEmpty()){
            Log.i(TAG, "onSaveInstanceState: saving images");
            outState.putStringArrayList(PICTURES_PATHS_LIST_NAME, picturesPaths);
            outState.putStringArrayList(PICTURES_NAMES_LIST_NAME,picturesNames);
        }
        if (point != null){
            Log.i(TAG, "onSaveInstanceState: saving point");
            outState.putParcelable(POINT_NAME,point);
        }


        if (mapLocation != null) {
            Log.i(TAG, "onSaveInstanceState: map location not null");
            outState.putParcelable(MAPLOCATION_NAME,mapLocation);

        }
        else{
            Log.i(TAG, "onSaveInstanceState: mapLocation is null");

        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        Log.i(TAG, "onCreateView: called");
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null){
            if (!(NavHostFragment.findNavController(LocationAddFragment.this).popBackStack())){
                Toast.makeText(getContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                
            }
        }
        dataBase = DAOFactory.getLocationDAOOnline();

        binding = FragmentAddLocationBinding.inflate(inflater, container, false);
        Log.i(TAG, "onCreateView: maplocation :" + String.valueOf(mapLocation == null));
        Log.i(TAG, "onCreateView: images :" + String.valueOf(images == null));
        Log.i(TAG, "onCreateView: picturesCloudUri : "+String.valueOf(picturesCloudUris == null));
        Log.i(TAG, "onCreateView: point : "+String.valueOf(point == null));
        images = new ArrayList<>();
        picturesPaths = new ArrayList<>();
        picturesNames = new ArrayList<>();
        picturesCloudUris = new ArrayList<>();
        pictureTaken = false;
        viewPager = binding.locationAddViewPager;


        if (savedInstanceState != null) {

                Log.i(TAG, "onCreateView: savedinstancestate is not null");
                pictureTaken = savedInstanceState.getBoolean(PICTURE_TAKEN_NAME);
            if (savedInstanceState.containsKey(CURRENT_PICTURE_URI)){
                Log.i(TAG, "onCreateView: savedInstanceState contains currentPictureUri");
                currentPictureUri = savedInstanceState.getParcelable(CURRENT_PICTURE_URI);
            }
            if (savedInstanceState.containsKey(CURRENT_PICTURE_NAME)){
                Log.i(TAG, "onCreateView: savedInstanceState contains currentPictureName");
                currentPictureName = savedInstanceState.getString(CURRENT_PICTURE_NAME);
            }
            if (savedInstanceState.containsKey(CURRENT_PICTURE_PATH)){
                Log.i(TAG, "onCreateView: savedInstanceState contains currentPicturePath");
                currentPicTurePath = savedInstanceState.getString(CURRENT_PICTURE_PATH);
            }
                if (savedInstanceState.containsKey(PICTURES_PATHS_LIST_NAME)){
                    Log.i(TAG, "onCreateView: rebuilding images");
                    picturesPaths = savedInstanceState.getStringArrayList(PICTURES_PATHS_LIST_NAME);
                    picturesNames = savedInstanceState.getStringArrayList(PICTURES_NAMES_LIST_NAME);
                    if (picturesPaths == null|| picturesNames == null){
                        Log.i(TAG, "onCreateView: unparceled images datas is null");
                    }
                    else{
                        for (int i = 0; i < picturesPaths.size(); i++){

                            images.add(new SliderItem(BitmapFactory.decodeFile(picturesPaths.get(i)),picturesNames.get(i),picturesPaths.get(i)));
                        }
                        showPictures();
                    }


                }
                if (savedInstanceState.containsKey(MAPLOCATION_NAME)){
                    Log.i(TAG, "onCreateView: rebuilding mapLocation");
                    mapLocation = (MapLocation) savedInstanceState.getParcelable(MAPLOCATION_NAME);


                }
                if (savedInstanceState.containsKey(POINT_NAME)){
                    Log.i(TAG, "onCreateView: rebuilding point");
                    point = (LatLng) savedInstanceState.getParcelable(POINT_NAME);
                    try {
                        showLocationDatas();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }


        }
        else{
            Log.i(TAG, "onCreateView: savedinstancestate is null");


            if (getArguments() != null) {


                try {
                    point = ((LatLng) getArguments().getParcelable(MapsFragment.MAP_POINT_EXTRA_NAME));
                    showLocationDatas();

                } catch (Exception e) {
                    //TODO : trier les exceptions pour le toast, détecter timeout (exception levée si problème réseau check "grpc failed")
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(getContext(), getString(R.string.network_error), Toast.LENGTH_LONG).show();
                    NavHostFragment.findNavController(LocationAddFragment.this).popBackStack();
                }

            } else {

                Log.i(TAG, "onCreateView: direct location add, locating user...");
                point = new LatLng(0, 0);
                new UserLocalisation(getContext()).locateUser(task -> {
                    Log.i(TAG, "onCreateView: location task received ");
                    if (task.isSuccessful()) {
                        Log.i(TAG, "onCreateView: user localisation successful");
                        point = new LatLng(task.getResult().getLatitude(), task.getResult().getLongitude());
                        try {
                            showLocationDatas();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.i(TAG, "onCreateView: failed to locate user");
                        Toast.makeText(getContext(), getString(R.string.no_localisation_permissions), Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
                    }
                });
                Log.i(TAG, "onCreateView: user location called");
                Log.i(TAG, "onViewCreated: images size : " + images.size());

                Log.i(TAG, "onCreateView: location add started from map");


            }




            /** test uniquement :*/
            //TODO si temps : créer layouts différentes tailles
//            images.add(0,new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.test_),EMPTY_VIEWPAGER_PICTURE_NAME));
//            pictureTaken = true;
//            images.add(new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.test_),getContext().getResources().getDrawable(R.drawable.test_).toString()));
//            images.add(new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.add_picture),getContext().getResources().getDrawable(R.drawable.add_picture).toString()));
//            images.add(new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.no_picture),getContext().getResources().getDrawable(R.drawable.no_picture).toString()));
//            showPictures();
            /**
             * fin partie test
             */
        }

        if (pictureTaken && !images.isEmpty()){
            enablePictureDelete(true);
        }
        else{
            showIllustrationPicture();
        }
        if (binding.addWaterCheckBox.isChecked()) {
            binding.addDrinkableCheckBox.setEnabled(true);
        } else {
            binding.addDrinkableCheckBox.setEnabled(false);
        }
        if (binding.addInternetCheckBox.isChecked()){
            binding.addInternetTypeRadioGroup.setVisibility(View.VISIBLE);
        }
        else{
            binding.addInternetTypeRadioGroup.setVisibility(View.INVISIBLE);
        }
        if (binding.addPrivateNetworkRadioButton.isChecked()) {
            binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
            binding.internetPriceEditText.setVisibility(View.VISIBLE);
        } else {
            binding.addInternetPriceLabel.setVisibility(View.GONE);
            binding.internetPriceEditText.setVisibility(View.GONE);
        }
        if (binding.addDrainageCheckbox.isChecked()) {
            binding.addDarkWaterCheckBox.setVisibility(View.VISIBLE);
        } else {
            binding.addDarkWaterCheckBox.setVisibility(View.GONE);
        }
        binding.addWaterCheckBox.setOnClickListener(view -> {
            if (((CheckBox) view).isChecked()) {
                binding.addDrinkableCheckBox.setEnabled(true);
            } else {
                binding.addDrinkableCheckBox.setEnabled(false);
            }
        });
        binding.addInternetCheckBox.setOnClickListener(view -> {
            if (((CheckBox) view).isChecked()) {
                binding.addInternetTypeRadioGroup.setVisibility(View.VISIBLE);
            } else {
                binding.addInternetTypeRadioGroup.setVisibility(View.INVISIBLE);
            }
        });
        binding.addInternetTypeRadioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            Log.i(TAG, "RadioGroup onCheckedChangeListener called; i = " + i);
            if (binding.addPublicWifiRadioButton.isChecked()) {
                binding.addInternetPriceLabel.setVisibility(View.GONE);
                binding.internetPriceEditText.setVisibility(View.GONE);
            } else {
                binding.addInternetPriceLabel.setVisibility(View.VISIBLE);
                binding.internetPriceEditText.setVisibility(View.VISIBLE);
            }
        });
        binding.addDrainageCheckbox.setOnClickListener(view -> {
            if (binding.addDrainageCheckbox.isChecked()) {
                binding.addDarkWaterCheckBox.setVisibility(View.VISIBLE);
            } else {
                binding.addDarkWaterCheckBox.setVisibility(View.GONE);
            }
        });
        return binding.getRoot();

    }
    private void showIllustrationPicture(){
        if (!pictureTaken && images.isEmpty()) {
            Log.i(TAG, "onCreateView: adding illustration picture");
            images.add(new SliderItem(BitmapFactory.decodeResource(getResources(), R.drawable.add_picture), EMPTY_VIEWPAGER_PICTURE_NAME));

        }
        viewPager.setAdapter(new SliderAdapter(images,viewPager));
    }

    private void enablePictureDelete(boolean enabled){
        if (enabled){
            binding.pictureDeleteButton.setVisibility(View.VISIBLE);
            binding.pictureDeleteButton.setOnClickListener(view ->{
                Log.i(TAG, "onCreateView: delete button clicked");
                int item = viewPager.getCurrentItem();
                Log.i(TAG, "onCreateView: viewPager item = "+ item);
                if (item < images.size()){
                    images.remove(item);
                    picturesNames.remove(item);
                    picturesPaths.remove(item);
                    freePictureSpace[item] = true;
                    if (images.isEmpty()){

                        binding.pictureDeleteButton.setVisibility(View.GONE);
                        pictureTaken = false;
                        viewPager.unregisterOnPageChangeCallback(onPageChangeCallback);
                    }

                }

            });
        }
        else{
            binding.pictureDeleteButton.setVisibility(View.GONE);
        }
    }
    private void showLocationDatas() throws Exception{
        if (point != null) {
            locationId = MapLocation.Builder.generateId(point);
            Log.i(TAG, "onCreateView: intent extras: " + point.toString());
            binding.locationAddTextviewCoordinates.setText(Double.toString(point.latitude)+" | "+Double.toString(point.longitude));



            /*
             * récupération du nom du lieu
             */
            Geocoder geocoder = new Geocoder(getContext());
            List<Address> addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1);
            Address address = addresses.get(0);

            Log.i(TAG, "onViewCreated: address 0 = " + address.getAddressLine(0));
            if (address.getFeatureName() != null && address.getFeatureName().length() > 8) {
                name = address.getFeatureName();
                Log.i(TAG, "onViewCreated: adress feature name :" + name);
            } else {
                name = address.getAddressLine(0);
            }
        }else{
            Toast.makeText(getContext(), getString(R.string.coordinates_missing_error), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(LocationAddFragment.this).navigate(R.id.action_AddLocationFragment_to_MenuFragment);
        }
        /*
         * modification du titre
         */
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null){
            Log.i(TAG, "onViewCreated: main activity not null");
            ActionBar actionBar = mainActivity.getSupportActionBar();
            if (actionBar != null){
                Log.i(TAG, "onViewCreated: action bar not null");
                actionBar.setTitle(name);

            }
            else{ Log.i(TAG, "onViewCreated: action bar is null");}

            Log.i(TAG, "onCreateView: mainActivity is not null, hiding settings");
            mainActivity.setSettingsVisibility(false);

        }
        else Log.i(TAG, "onViewCreated: mainActivity is null");



    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);

//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        binding.takePictureButton.setOnClickListener(button -> {
            //File file = File.createTempFile(name,".jpg");
//            mGetcontent.launch(location.getUri());


            if (images.size() < 3) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.add_picture_title))
                        .setMessage(getString(R.string.picture_select_type))
                        .setPositiveButton(R.string.take_a_picture, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    takePicture();
                                } catch (IOException e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG);
                                }
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(R.string.pick_a_picture, (dialogInterface, i) -> {
                            pickPicture();
                        })
                        .setIcon(R.mipmap.ic_picture_add_mode)
                        .show();
            } else {
                Toast.makeText(getContext(), getString(R.string.exceed_pictures_count), Toast.LENGTH_SHORT).show();

            }
        });


        binding.buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NavHostFragment.findNavController(LocationAddFragment.this)
//                        .navigate(R.id.action_AddLocationFragment_to_MenuFragment);


                mapLocation = processInputs();
                if (mapLocation != null) {
                    if (pictureTaken) {
                        uploadPictures(mapLocation.getUser_id(), mapLocation.getId());
                    } else {

                        mapLocation.setPictures(null);
                        dataBase.insert(mapLocation,LocationAddFragment.this);
                    }
                }

            }
        });
    }
    private int getPicturesCount(){
        if (images != null){
            if (images.isEmpty()){
                return 1;
            }
            for (int i =0;i < freePictureSpace.length;i++){
                if (freePictureSpace[i]){
                    return i +1;
                }
            }

        }
        return -1;
    }
    private void uploadPictures(String userId,String locationId){
        Log.i(TAG, "uploadPictures called, "+images.size()+" pictures to upload");
        Log.i(TAG, "uploadPictures called thread = " + Thread.currentThread().toString());

        PicturesUploadTask picturesUploadTask = new PicturesUploadTask(images,this);
        picturesUploadTask.execute(userId,locationId);

    }
    private MapLocation processInputs(){
        String description;
        Map<String, Service> services = new HashMap<>();
        if (binding.addDescriptionEditText.getText().length() < 4){
            Toast.makeText(getContext(), getString(R.string.empty_description), Toast.LENGTH_SHORT).show();
            return null;
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
                    return null;
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
                    return null;
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
                        return null;
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
        if (service) {
            return new MapLocation(point.latitude,point.longitude,description,services,user.getUid(),name,null,false);
        }
        else{
            Toast.makeText(getContext(), getString(R.string.no_service_selected), Toast.LENGTH_LONG).show();
            return null;
        }

    }
    public List<Bitmap> getPictures(){
        final List<Bitmap> list = new ArrayList<>();
        if (pictureTaken){

            images.forEach(sliderItems -> {
                list.add(sliderItems.getImage());
            });
            return list;
        }
        return null;
    }
    private double parsePrice(EditText editText,String name) throws NumberFormatException{
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
    private void pickPicture(){
        pickPictureContract.launch("image/*");
    }


    private void takePicture() throws IOException {
//        File mediaStorageDir = new File(getContext().getFilesDir(), "Service4night pics");

        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), PictureDownloader.PICTURES_LOCAL_FOLDER);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
            Log.d(TAG, "failed to create directory");
        }
        Log.i(TAG, "takePicture: file path is: "+ mediaStorageDir.getPath());
        // Return the file target for the photo based on filename
        int pictureCount;
        if ((pictureCount = getPicturesCount()) != -1){
            currentPictureName = MapLocation.generatePictureName(locationId, pictureCount);
            currentPicTurePath = mediaStorageDir.getPath() + File.separator + currentPictureName;
            File file = new File(currentPicTurePath);
            currentPictureUri = FileProvider.getUriForFile(getContext(), "fr.abitbol.service4night.fileprovider", file);

            mGetcontent.launch(currentPictureUri);
        }
        else{
            Toast.makeText(getContext(), getString(R.string.exceed_pictures_count), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView called");
        binding = null;
    }
    @Override
    public void startProgressBar(){

        binding.addFrameLayout.setEnabled(false);
        binding.addProgressBarContainer.setVisibility(View.VISIBLE);
        binding.addProgressBarContainer.setEnabled(true);
        //binding.addProgressBar.animate();
        binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format),1,images.size()));
    }
    @Override
    public void stopProgressBar(){
        binding.addFrameLayout.setEnabled(true);
        binding.addProgressBarContainer.setVisibility(View.GONE);
    }
    @Override
    public void updateProgressBar(boolean success,int done) {
        if (success) {
            Log.i(TAG, "updateProgressBar:  picture upload success");
            binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format), done, images.size()));
        }
        else {
            Log.i(TAG, "updateProgressBar: picture upload failed");
            binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format), done, images.size()));
            Toast.makeText(getContext(), String.format(getString(R.string.progressBar_picture_failed),(done-1)), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPictures(){


        viewPager.setClipToPadding(false);
        viewPager.setClipChildren(false);
        viewPager.setOffscreenPageLimit(2);
        int offsetPx = Math.round(getResources().getDisplayMetrics().density * 20);

        viewPager.setPadding(offsetPx, 0, offsetPx, 0);

        viewPager.setAdapter(new SliderAdapter(images, viewPager));
        enablePictureDelete(true);
        viewPager.invalidate();

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

    @Override
    public void onPicturesUploaded(List<String> uris) {
        if (uris != null && !uris.isEmpty()) {
            Log.i(TAG, "onPicturesUploaded: pictures successfully uploaded: ");
            picturesCloudUris = uris;
            for (String uri : picturesCloudUris) {
                Log.i(TAG, "uploaded : toString: " + uri );
            }
            mapLocation.setPictures(picturesCloudUris);

        }
        else{
            //TODO si temps : proposer rééssayer upload
            Log.i(TAG, "onPicturesUploaded: uri list is null or empty");
            Toast.makeText(getContext(), getString(R.string.location_added_no_pictures), Toast.LENGTH_LONG).show();
            mapLocation.setPictures(null);
        }
        dataBase.insert(mapLocation,this);
    }


}