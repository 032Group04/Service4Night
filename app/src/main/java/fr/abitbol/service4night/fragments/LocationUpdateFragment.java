package fr.abitbol.service4night.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

import fr.abitbol.service4night.DAO.CloudStoragePictureDAO;
import fr.abitbol.service4night.DAO.DAOFactory;
import fr.abitbol.service4night.DAO.LocationDAO;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.FragmentAddLocationBinding;
import fr.abitbol.service4night.listeners.OnPictureDownloadListener;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;
import fr.abitbol.service4night.utils.ExifUtil;
import fr.abitbol.service4night.utils.PictureDownloader;
import fr.abitbol.service4night.utils.PicturesUploadAdapter;
import fr.abitbol.service4night.utils.SliderAdapter;
import fr.abitbol.service4night.utils.SliderItem;


public class LocationUpdateFragment extends Fragment implements OnCompleteListener<Void>, OnPictureDownloadListener,PicturesUploadAdapter {



    private FragmentAddLocationBinding binding;

    private final String TAG = "LocationUpdateFragment logging";
    private static final String PICTURE_TAKEN_NAME = "pictureTaken";

    public static final String MAPLOCATION_NAME = "mapLocation";

    private MapLocation mapLocation;
    ArrayList<SliderItem> images;
    private Uri currentPictureUri;
    private LocationDAO dataBase;
    private ViewPager2 viewPager;
    private FirebaseUser user;
    private boolean pictureTaken;

    private String currentPicTurePath;
    private String currentPictureName;
    private Integer currentPictureSpace;

    //TODO: problème écran noir depuis mapActivity quand réseau faible

    private ActivityResultLauncher<Uri> takePictureContract = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {



        @Override
        public void onActivityResult(Boolean result) {
            Log.i(TAG, "onActivityResult: picture result : "+result);
            if (result){
                //binding.pictureAddLayout.setBackground(null);
                //binding.imageView.setImageURI(uri);


                try {
                    unlockScreen();
                    Bitmap picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), currentPictureUri);
                    pictureTaken = true;
                    File file = new File(currentPicTurePath);
                    Log.i(TAG, "onActivityResult: file exists :" + file.exists());
                    picture = ExifUtil.rotateBitmap(currentPicTurePath,picture);
                    picture = ExifUtil.resizeBitmap(picture);
                    images.add(new SliderItem(picture,currentPictureName, currentPicTurePath));
                    showPictures();
                    updatePicture(currentPictureSpace,picture);

                    enablePictureDelete(true);



                } catch (IOException e) {
                    Toast.makeText(getContext(), "error while getting bitmap from uri", Toast.LENGTH_LONG).show();
                }

            }
        }
    });
    private ActivityResultLauncher<String> pickPictureContract = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
        @Override
        public void onActivityResult(Uri result) {
            try {
                Bitmap picture = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), result);
                pictureTaken = true;
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
                currentPictureName = MapLocation.generatePictureName(mapLocation.getId(), getPictureSpace() + 1);
                images.add(new SliderItem(picture,currentPictureName, currentPicTurePath));
                showPictures();
                updatePicture(getPictureSpace(),picture);

                enablePictureDelete(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    });
    //TODO : ajouter bouton supprimer photo
    public synchronized void updatePicture(int index,Bitmap bitmap){
        Log.i(TAG, "updatePicture: index : " + index);
        CloudStoragePictureDAO pictureDAO = new CloudStoragePictureDAO();
        pictureDAO.registerInsertListener(task -> {
            if (task.isSuccessful()){
                if (index >= mapLocation.getPictures().size()){
                    Log.i(TAG, "updatePicture: adding picture in empty list space");
                    mapLocation.getPictures().add(task.getResult().toString());
                }
                else {
                    Log.i(TAG, "updatePicture: adding picture in list index "+ index);
                    mapLocation.getPictures().set(index, task.getResult().toString());
                }
                currentPicTurePath = null;
                currentPictureUri = null;
                currentPictureSpace = null;
                getActivity().runOnUiThread(this::stopProgressBar);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.upload_picture_success), Toast.LENGTH_SHORT).show());
            }
            else{
                getActivity().runOnUiThread(this::stopProgressBar);
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.upload_picture_fail), Toast.LENGTH_SHORT).show());
            }
        });
        startProgressBar();
        new Thread(() ->{
            pictureDAO.insert(currentPictureName,mapLocation.getUser_id(),mapLocation.getId(),bitmap);

        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onResume() {
        Log.i(TAG, "onResume called ");
        super.onResume();

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(PICTURE_TAKEN_NAME,pictureTaken);




        if (mapLocation != null) {
            Log.i(TAG, "onSaveInstanceState: map location not null");
            outState.putParcelable(MAPLOCATION_NAME,mapLocation);

        }
        else{
            Log.i(TAG, "onSaveInstanceState: mapLocation is null");
            Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();        }
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

            Toast.makeText(getContext(), getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();


        }
        dataBase = DAOFactory.getLocationDAOReadAndWrite(LocationUpdateFragment.this, true);

        binding = FragmentAddLocationBinding.inflate(inflater, container, false);

        images = new ArrayList<>();


        pictureTaken = false;
        viewPager = binding.locationAddViewPager;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MAPLOCATION_NAME)){
                Log.i(TAG, "onCreateView: rebuilding mapLocation");
                mapLocation = (MapLocation) savedInstanceState.getParcelable(MAPLOCATION_NAME);
                if (mapLocation != null) {
                    loadInputs();
                    loadPictures();

                }
                else{
                    Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();
                }

            }
            else{
                Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();
            }

            Log.i(TAG, "onCreateView: savedinstancestate is not null");
            pictureTaken = savedInstanceState.getBoolean(PICTURE_TAKEN_NAME);






        }
        else{
            Log.i(TAG, "onCreateView: savedinstancestate is null");


            if (getArguments() != null) {


                mapLocation = getArguments().getParcelable(MAPLOCATION_NAME);
                if (mapLocation != null){
                    loadInputs();
                    loadPictures();
                    if (mapLocation.getPictures() == null){
                        Log.i(TAG, "onCreateView: modified location doesn't have pictures");

                    }

                }
                else{
                    Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();
                }


            }
            else{
                Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();
            }


            viewPager.setAdapter(new SliderAdapter(images, viewPager));


            /** test uniquement :*/

            //images.add(0,new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.test_),getContext().getResources().getDrawable(R.drawable.test_).toString()));

            /**
             * fin partie test
             */
        }
        setTitle();
        if (!images.isEmpty()){
            enablePictureDelete(true);
        }
        if (binding.addWaterCheckBox.isChecked()) {
            binding.addDrinkableCheckBox.setEnabled(true);
        } else {
            binding.addDrinkableCheckBox.setEnabled(false);
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

    private void loadPictures(){
        if (mapLocation.getPictures()!= null && mapLocation.getPictures().size() > 0) {
            PictureDownloader.getBitmapsFromURL(mapLocation,new ArrayList<String>(),new ArrayList<String>(),getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),this);
        } else {
            Log.i(TAG, "loadPictures: no pictures in mapLocation");
        }
    }
    private void loadInputs(){
        binding.locationAddTextviewLatitude.setText(String.valueOf(mapLocation.getPoint().latitude));
        binding.locationAddTextviewLongitude.setText(String.valueOf(mapLocation.getPoint().longitude));
        binding.addDescriptionEditText.setText(mapLocation.getDescription());
        Map<String, Service> services = mapLocation.getServices();

        if (services.containsKey(Service.WATER_SERVICE)) {
            WaterService waterService = (WaterService) services.get(Service.WATER_SERVICE);
            binding.addWaterCheckBox.setChecked(true);
            binding.addWaterPriceEditText.setText(String.valueOf(waterService.getPrice()));
            if (waterService.isDrinkable()) {

                binding.addDrinkableCheckBox.setChecked(true);

            } else {
                binding.addDrinkableCheckBox.setChecked(false);
            }




        }

        if (services.containsKey(Service.ELECTRICITY_SERVICE)) {
            ElectricityService electricityService = (ElectricityService) services.get(Service.ELECTRICITY_SERVICE);
            binding.addElectricityCheckBox.setChecked(true);
            binding.electricityPriceEditText.setText(String.valueOf(electricityService.getPrice()));

        }
        if (services.containsKey(Service.DUMPSTER_SERVICE)) {
            binding.addDumpsterCheckBox.setChecked(true);

        }

        if (services.containsKey(Service.INTERNET_SERVICE)) {
            InternetService internetService = (InternetService) services.get(Service.INTERNET_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of internet service");

            if (internetService.getConnectionType().equals(InternetService.ConnectionType.public_wifi)) {

                binding.addPublicWifiRadioButton.setChecked(true);
                binding.addPrivateNetworkRadioButton.setChecked(false);

            } else {
                binding.addPublicWifiRadioButton.setChecked(false);
                binding.addPrivateNetworkRadioButton.setChecked(true);
                binding.internetPriceEditText.setText(String.valueOf(internetService.getPrice()));

            }



        }

        if (services.containsKey(Service.DRAINAGE_SERVICE)) {
            DrainService drainService = (DrainService) services.get(Service.DRAINAGE_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of drain service");
            binding.addDrainageCheckbox.setChecked(true);
            if (drainService.isBlackWater()){
                binding.addDarkWaterCheckBox.setChecked(true);
            }
            else{
                binding.addDarkWaterCheckBox.setChecked(false);
            }
        }

    }
    public void lockScreen(){
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    public void unlockScreen(){
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }
    private void enablePictureDelete(boolean enabled){
        if (enabled){
            binding.deleteButton.setVisibility(View.VISIBLE);
            binding.deleteButton.setOnClickListener(view ->{

                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.delete_picture_title))
                        .setMessage(getString(R.string.confirm_delete_picture))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onCreateView: delete button clicked");
                                int item = viewPager.getCurrentItem();
                                Log.i(TAG, "onCreateView: viewPager item = "+ item);
                                if (item < images.size()){
                                    CloudStoragePictureDAO pictureDAO = new CloudStoragePictureDAO();
                                    if(pictureDAO.delete(mapLocation.getPictures().get(item))){
                                        Log.i(TAG, "enablePictureDelete: picture successfully deleted");
                                        images.remove(item);
                                        String tmp = mapLocation.getPictures().get(item);
                                        mapLocation.getPictures().set(item,PictureDownloader.DELETED_PICTURES_VALUE);
                                        viewPager.setAdapter(new SliderAdapter(images, viewPager));
                                        Log.i(TAG, "picture delete: onClick: picture space freed :"+item);
                                        LocationDAO locationDAO = DAOFactory.getLocationDAOReadAndWrite(task -> {
                                            if (task.isSuccessful()){
                                                Toast.makeText(getContext(), getString(R.string.picture_delete_success), Toast.LENGTH_SHORT).show();

                                            }
                                            else{
                                                Log.i(TAG, "onClick: failed to modify picture status in database");
                                                Toast.makeText(getContext(), getString(R.string.picture_delete_fail), Toast.LENGTH_SHORT).show();


                                            }
                                        },true);
                                    }
                                    else{
                                        Log.i(TAG, "onClick: picture delete failed");
                                        Toast.makeText(getContext(), getString(R.string.picture_delete_fail), Toast.LENGTH_SHORT).show();

                                    }

                                    if (images.isEmpty()){
                                        binding.deleteButton.setVisibility(View.GONE);
                                        mapLocation.setPictures(null);
                                        pictureTaken = false;
                                    }
                                }

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();



            });
        }
        else{
            binding.deleteButton.setVisibility(View.GONE);
        }
    }
    private void setTitle() {


            /*
             * modification du titre
             */
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity != null) {
                Log.i(TAG, "onViewCreated: main activity not null");
                ActionBar actionBar = mainActivity.getSupportActionBar();
                if (actionBar != null) {
                    Log.i(TAG, "onViewCreated: action bar not null");
                    actionBar.setTitle(mapLocation.getName());

                } else {
                    Log.i(TAG, "onViewCreated: action bar is null");
                }

                Log.i(TAG, "onCreateView: mainActivity is not null, hiding settings");
                mainActivity.setSettingsVisibility(false);

            } else Log.i(TAG, "onViewCreated: mainActivity is null");



    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.i(TAG, "onViewCreated: called");
        super.onViewCreated(view, savedInstanceState);

//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        binding.takePictureButton.setOnClickListener(button -> {
            //File file = File.createTempFile(name,".jpg");
//            mGetcontent.launch(location.getUri());
            if (mapLocation.getPictures() == null){
                Log.i(TAG, "onViewCreated: location has no pictures");
                mapLocation.setPictures(new ArrayList<>());
            }
            if (getPictureSpace() != null ) {
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
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            } else {
                Toast.makeText(getContext(), getString(R.string.exceed_pictures_count), Toast.LENGTH_SHORT).show();

            }

        });

        //TODO : ajouter possibilité de selectionner une photo dans le téléphone

        binding.buttonValidate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(processInputs()) {
                    dataBase.insert(mapLocation);
                }


            }
        });
    }
    private Integer getPictureSpace(){
        if(mapLocation.getPictures().size() < 3 ){
            return ((mapLocation.getPictures().size()));
        }
        else{
            for (int i = 0; i < mapLocation.getPictures().size(); i++){
                if (mapLocation.getPictures().get(i).equals(PictureDownloader.DELETED_PICTURES_VALUE)){
                    return  i;
                }
            }
            Log.i(TAG, "getPictureSpace: no picture space");
        }

        return null;

    }

    private boolean processInputs(){
        boolean servicePresent = false;
        String description = mapLocation.getDescription();
        Map<String, Service> services = new HashMap<>();
        if (binding.addDescriptionEditText.getText().length() < 4){
            Toast.makeText(getContext(), getString(R.string.empty_description), Toast.LENGTH_SHORT).show();
            return false;
        }
        else{

            description =binding.addDescriptionEditText.getText().toString();
            Log.i(TAG, "onClick: description = "+ mapLocation.getDescription());
        }



        if (binding.addWaterCheckBox.isChecked()){
            servicePresent = true;
            boolean drinkable = binding.addDrinkableCheckBox.isChecked();

            Log.i(TAG, "processServices: water checked : drikable = "+ drinkable);
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
            services.put(Service.WATER_SERVICE,new WaterService(price,drinkable));


        }
        if (binding.addElectricityCheckBox.isChecked()){
            servicePresent = true;
            Log.i(TAG, "processServices: electricity checked");

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
            services.put(Service.ELECTRICITY_SERVICE,new ElectricityService(price));


        }
        if (binding.addInternetCheckBox.isChecked()){
            servicePresent = true;
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
            Log.i(TAG, "processServices: internet checked : connection type = "+ connectionType.name());

            if (connectionType.equals(InternetService.ConnectionType.public_wifi)) {
                services.put(Service.INTERNET_SERVICE, new InternetService(connectionType));
            }
            else{
                services.put(Service.INTERNET_SERVICE,new InternetService(connectionType,price));
            }


        }
        if (binding.addDumpsterCheckBox.isChecked()){
            servicePresent = true;
            services.put(Service.DUMPSTER_SERVICE,new DumpService());
        }
        if (binding.addDrainageCheckbox.isChecked()){
            servicePresent = true;

            boolean blackWater = binding.addDarkWaterCheckBox.isChecked();
            Log.i(TAG, "processInputs: drainage service checked , blackwater = " + blackWater);

            services.put(Service.DRAINAGE_SERVICE,new DrainService(blackWater));

        }
        if (servicePresent) {
            mapLocation.setDescription(description);
            mapLocation.setServices(services);
            return true;
        }
        else{
            Log.i(TAG, "processInputs: no service selected, no changes were made ");
            Toast.makeText(getContext(), getString(R.string.update_no_service_selected), Toast.LENGTH_SHORT).show();
            return false;
        }


    }

    private double parsePrice(EditText editText, String name) throws NumberFormatException{
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

        if ((currentPictureSpace = getPictureSpace()) != null){
            currentPictureName = MapLocation.generatePictureName(mapLocation.getId(),(currentPictureSpace + 1));
            currentPicTurePath = mediaStorageDir.getPath() + File.separator + currentPictureName;
            File file = new File(currentPicTurePath);
            currentPictureUri = FileProvider.getUriForFile(getContext(),"fr.abitbol.service4night.fileprovider",file);

            takePictureContract.launch(currentPictureUri);
            lockScreen();
        }
        else{
            Toast.makeText(getContext(), getString(R.string.exceed_pictures_count), Toast.LENGTH_SHORT).show();
        }



    }
    public void resume(MapLocation mapLocation){
        //TODO : afficher les données de mapLocation pour récupérer ajout abandonné

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
        binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format),1,1));
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
            binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format), done, 1));
        }
        else {
            Log.i(TAG, "updateProgressBar: picture upload failed");
            binding.addProgressBarTextView.setText(String.format(getString(R.string.progressBar_text_format), done, 1));
            Toast.makeText(getContext(), String.format(getString(R.string.progressBar_picture_failed),(done-1)), Toast.LENGTH_SHORT).show();
        }
    }
    private void showLoadScreen(){
        binding.addFrameLayout.setEnabled(false);
        binding.addProgressBarContainer.setVisibility(View.VISIBLE);
        binding.addProgressBarTextView.setText(R.string.loading_pictures);
    }
    private void hideLoadScreen(){
        binding.addFrameLayout.setEnabled(true);
        binding.addProgressBarContainer.setVisibility(View.GONE);
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
            NavHostFragment.findNavController(LocationUpdateFragment.this).popBackStack();
        }
        else{
            Log.i(TAG, "onComplete: location failed to be written");
            Log.i(TAG, "onComplete: task to string : " + task.toString());
            Log.i(TAG, "onComplete: task get Exception : "+ task.getException());
            Toast.makeText(getContext(), getString(R.string.location_add_fail), Toast.LENGTH_SHORT).show();
        }

    }



    @Override
    public void onPictureDownload(ArrayList<SliderItem> _images) {
        if (_images != null) {
            Log.i(TAG, "onPictureDownload: download complete : "+ _images.size()+ " images");
            images = _images;
            getActivity().runOnUiThread(this::showPictures);
            getActivity().runOnUiThread(this::hideLoadScreen);
        }
    }

    @Override
    public void onPicturesUploaded(List<String> uris) {

    }
}