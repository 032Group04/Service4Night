package fr.abitbol.service4night.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import fr.abitbol.service4night.FullScreenPictureSlideActivity;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.MapsActivity;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.listeners.OnPictureDownloadListener;
import fr.abitbol.service4night.utils.ExifUtil;
import fr.abitbol.service4night.utils.PictureDownloader;
import fr.abitbol.service4night.utils.SliderAdapter;
import fr.abitbol.service4night.utils.SliderItem;
import fr.abitbol.service4night.databinding.FragmentLocationBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationFragment extends Fragment implements OnPictureDownloadListener {


    private static final String TAG = "LocationFragment logging";
    public static final String EXTRA_PICTURES_PATHS = "picturesPaths";
    public static final String EXTRA_PICTURES_NAMES = "picturesNames";
    public static final String IMAGES_NAME = "images";
    public static final String MAPLOCATION_NAME = "mapLocation";
    public static final String POINT_NAME = "point";
    public static final String PICTURE_PATH_NAME = "picturePath";
    public static final String PICTURE_URI_NAME = "userPictureUri";
    private FragmentLocationBinding binding;

    private MapLocation mapLocation;
    private ArrayList<SliderItem> images;
    boolean[][] boxGrid;
    private ViewPager2 viewPager;
    private ArrayList<String> picturesPaths;
    private ArrayList<String> picturesNames;

    public LocationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(MAPLOCATION_NAME,mapLocation);


        if (images != null && images.size() == mapLocation.getPictures().size()){
            Log.i(TAG, "onSaveInstanceState: images are fully loaded, saving images");

            outState.putStringArrayList(EXTRA_PICTURES_PATHS,picturesPaths);
            outState.putStringArrayList(EXTRA_PICTURES_NAMES,picturesNames);
        }
        else{
            Log.i(TAG, "onSaveInstanceState: images are not fully loaded, reloading necessary");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called ");
        images = null;
        picturesPaths = null;
        picturesNames = null;
        boxGrid = new boolean[3][3];
        for (int y = 0;y < 3;y++){
            for (int x = 0; x < 3; x++){
                boxGrid[x][y] = true;
            }
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called");
        binding = FragmentLocationBinding.inflate(inflater,container,false);
        images = new ArrayList<>();
        picturesNames = new ArrayList<>();
        picturesPaths = new ArrayList<>();
        viewPager = binding.locationViewPager;
        if (savedInstanceState != null) {
            Log.i(TAG, "onCreateView: savedinstancestate present");
            mapLocation = savedInstanceState.getParcelable(MAPLOCATION_NAME);
            if (savedInstanceState.containsKey(EXTRA_PICTURES_NAMES)){
                Log.i(TAG, "onCreateView: saveInstanceState contains images");

                picturesNames = savedInstanceState.getStringArrayList(EXTRA_PICTURES_NAMES);
                picturesPaths = savedInstanceState.getStringArrayList(EXTRA_PICTURES_PATHS);
                if (picturesPaths == null|| picturesNames == null){
                    Log.i(TAG, "onCreateView: unparceled images datas is null");
                    //TODO gérer problème images
                }
                else{
                    for (int i = 0; i < picturesPaths.size(); i++){

                        images.add(new SliderItem(BitmapFactory.decodeFile(picturesPaths.get(i)),picturesNames.get(i),picturesPaths.get(i)));
                    }

                }

            }
            else{
                Log.i(TAG, "onCreateView: no images in savedInstanceState");

            }
        }
        else {
            if (getArguments() != null) {
                try {
                    mapLocation = ((MapLocation) getArguments().getParcelable(MapsActivity.MAPLOCATION_EXTRA_NAME));
                    if (mapLocation != null) {
                        Log.i(TAG, "onCreateView: maplocation parcelable is not null");
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();

                    NavHostFragment.findNavController(LocationFragment.this).popBackStack();
                }
            } else {
                Log.i(TAG, "onCreateView: error : no MapLocation in arguments");
                Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(LocationFragment.this).popBackStack();

            }


        }
        if (mapLocation == null) {
            Log.i(TAG, "onViewCreated: mapLocation is null");
            Toast.makeText(getContext(), getString(R.string.location_retrieve_error), Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(LocationFragment.this).popBackStack();
        }

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

        }


        Log.i(TAG, "getInfoContents: mapLocation is not null");
        binding.locationNameTextView.setText(mapLocation.getName());
        binding.locationDescriptionEditText.setText(mapLocation.getDescription());
        Log.i(TAG, "getInfoContents: description is : " + mapLocation.getDescription());
        binding.locationCoordinatesTextView.setText(String.format(Locale.ENGLISH, "Lat : %f - Long : %f", mapLocation.getPoint().latitude, mapLocation.getPoint().longitude));

        if (!images.isEmpty() ){
            showPictures();
        }
        else {

            if (mapLocation.getPictures() != null && !mapLocation.getPictures().isEmpty()) {
                //TODO: transormer List<Uri> en Map et ajouter metadata nom picture
                //getBitmapsFromURL();
                downloadPictures();
            }
            else{
                Log.i(TAG, "onCreateView: location without pictures");
                //TODO : ajouter image d'illustration
            }
        }
        showServices();



        return binding.getRoot();
    }
    //TODO: bouton street view

    private void showServices() {
        Map<String, Service> services = mapLocation.getServices();

        if (services.containsKey(Service.WATER_SERVICE)) {
            WaterService waterService = (WaterService) services.get(Service.WATER_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of water service");

            CheckBox[] box = getCheckBoxesPosition(3);

            /*
            lignes de test
             */
//            for (CheckBox c : box) {
//                if (c == null) {
//                    Log.i(TAG, "onCreateView: checkbox is null");
//                    if (binding.locationCheckBox11 == null) {
//                        Log.i(TAG, "onCreateView: checkBox is null at source");
//                    }
//                }
//            }
            if (box != null) {
                box[0].setButtonDrawable(R.drawable.ic_service_water);
                box[0].setText(R.string.waterLabel);
                box[0].setVisibility(View.VISIBLE);

                Log.i(TAG, "onCreateView: setting drinkable");
                if (waterService.isDrinkable()) {

                    box[1].setButtonDrawable(R.drawable.ic_service_drinkable_water);
                    box[1].setText(R.string.drinkable_label);

                } else {
                    box[1].setButtonDrawable(R.drawable.ic_service_not_drinkable_water);
                    box[1].setText(R.string.not_drinkable_label);
                }
                box[1].setVisibility(View.VISIBLE);


                if (waterService.getPrice() == 0) {

                    box[2].setButtonDrawable(R.drawable.ic_service_free);
                    box[2].setText(getString(R.string.free_water));

                } else {
                    box[2].setButtonDrawable(R.drawable.ic_service_paying);
                    box[2].setText(String.valueOf(waterService.getPrice()) + " €");
                }
                box[2].setVisibility(View.VISIBLE);
            } else {
            }
        }

        if (services.containsKey(Service.ELECTRICITY_SERVICE)) {
            ElectricityService electricityService = (ElectricityService) services.get(Service.ELECTRICITY_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of electricity service");

            CheckBox[] box = getCheckBoxesPosition(2);
            if (box != null) {
                box[0].setText(R.string.electricityLabel);
                box[0].setButtonDrawable(R.drawable.ic_service_electricity);
                box[0].setVisibility(View.VISIBLE);


                if (electricityService.getPrice() == 0) {

                    box[1].setButtonDrawable(R.drawable.ic_service_free);
                    box[1].setText(getString(R.string.free_electricity));
                    box[1].setVisibility(View.VISIBLE);

                } else {
                    box[1].setButtonDrawable(R.drawable.ic_service_paying);
                    box[1].setText(String.valueOf(electricityService.getPrice()) + " €");
                    box[1].setVisibility(View.VISIBLE);
                }
            } else {

            }

        }
        if (services.containsKey(Service.DUMPSTER_SERVICE)) {
            Log.i(TAG, "getInfoContents:  instance of dump service");
            CheckBox[] boxArray =getCheckBoxesPosition(1);
            if (boxArray != null) {
                CheckBox box = boxArray[0];
                box.setText(R.string.dumpster_label);
                box.setButtonDrawable(R.drawable.ic_service_dumpster);
                box.setVisibility(View.VISIBLE);
            } else {
            }

        }

        if (services.containsKey(Service.INTERNET_SERVICE)) {
            InternetService internetService = (InternetService) services.get(Service.INTERNET_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of internet service");

            CheckBox[] box = getCheckBoxesPosition(2);

            if (box != null) {
                if (internetService.getConnectionType().equals(InternetService.ConnectionType.public_wifi)) {

                    box[0].setButtonDrawable(R.drawable.ic_service_wifi);
                    box[0].setText(R.string.wifi_label);

                } else {
                    box[0].setText(R.string.internet_label);
                    box[0].setButtonDrawable(R.drawable.ic_service_internet);
                }
                box[0].setVisibility(View.VISIBLE);


                if (internetService.getPrice() == 0) {

                    box[1].setButtonDrawable(R.drawable.ic_service_free);
                    box[1].setText(getString(R.string.free_internet));
                    box[1].setVisibility(View.VISIBLE);

                } else {
                    box[1].setButtonDrawable(R.drawable.ic_service_paying);
                    box[1].setText(String.valueOf(internetService.getPrice()) + " €");
                    box[1].setVisibility(View.VISIBLE);
                }
            }
            else {

            }

        }

        if (services.containsKey(Service.DRAINAGE_SERVICE)) {
            Service drainService = services.get(Service.DRAINAGE_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of drain service");
            CheckBox box = getCheckBoxesPosition(1)[0];
            box.setText((((DrainService) drainService).isBlackWater()) ? R.string.black_water_drain_label : R.string.grey_water_drain_label);
            box.setButtonDrawable(R.drawable.ic_service_drainage);
            box.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

            binding.buttonItinerary.setOnClickListener(v -> {
                Intent itineraryIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+mapLocation.getPoint().latitude+","+mapLocation.getPoint().longitude));
                itineraryIntent.setPackage("com.google.android.apps.maps");
                startActivity(itineraryIntent);
            });
            binding.buttonGoogleView.setOnClickListener(v ->{
                Intent searchIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" +mapLocation.getPoint().latitude +","+
                        mapLocation.getPoint().longitude +" ("+mapLocation.getName()+")"));
                searchIntent.setPackage("com.google.android.apps.maps");
                startActivity(searchIntent);
            });

    }

    public CheckBox getCheckbox(int count){
        Log.i(TAG, "getCheckbox: asked position : "+ count);
        switch (count){

            case 0 : return binding.locationCheckBox11;

            case 1 : return binding.locationCheckBox12;

            case 2 : return binding.locationCheckBox13;

            case 3 : return binding.locationCheckBox21;

            case 4 : return binding.locationCheckBox22;

            case 5 : return binding.locationCheckBox23;

            case 6 : return binding.locationCheckBox31;

            case 7 : return binding.locationCheckBox32;

            case 8 : return binding.locationCheckBox33;


        }
        Log.i(TAG, "getCheckbox: is returning null");
        return null;
    }

    private CheckBox[] getCheckBoxesPosition(int groupSize) {
        Log.i(TAG, "getPosition: "+groupSize+ " blocs asked");
        switch (groupSize) {
            case 0: {
                break;
            }
            case 1: {
                
                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (boxGrid[x][y]) {
                            Log.i(TAG, "getPosition: 1 free bloc on 1");
                            boxGrid[x][y] = false;
                            return new CheckBox[]{getCheckbox(x + y)};
                        }
                    }
                }

            }
            break;

            case 2: {

                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (boxGrid[x][y]) {
                            Log.i(TAG, "getPosition: 1 free bloc of 2");
                            if (x < 2){
                                Log.i(TAG, "getPosition: 2 free blocs on row x="+x);
                                if (boxGrid[x+1][y]){
                                    Log.i(TAG, "getPosition: enough space for 2");
                                    boxGrid[x][y] = false;
                                    boxGrid[x+1][y] = false;
                                    return new CheckBox[]{getCheckbox(x+y),getCheckbox(x+y+1)};
                                }else Log.i(TAG, "getPosition: next bloc is not free");
                            }
                            else Log.i(TAG, "getPosition: only 1 bloc free on row x="+x);
                        }
                    }
                }
            }
            break;
            case 3 :{

                for (int y = 0; y < 3; y++) {
                    for (int x = 0; x < 3; x++) {
                        if (boxGrid[x][y]) {
                            Log.i(TAG, "getPosition: 1 free bloc of 3");

                            if (x == 0 && boxGrid[(x+1)][y] && boxGrid[(x+2)][y]){
                                Log.i(TAG, "getPosition: complete line free");
                                boxGrid[x][y] = false;
                                boxGrid[(x+1)][y] = false;
                                boxGrid[(x+2)][y] = false;
                                return new CheckBox[]{getCheckbox((x+y)),getCheckbox((x+y+1)),getCheckbox((x+y+2))};

                            }else Log.i(TAG, "getPosition: line is not free");
                        }
                    }
                }
            }break;
            default: return null;

        }
        return null;
    }

    private void showPictures(){


            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(2);
            int offsetPx = Math.round(getResources().getDisplayMetrics().density * 20);

            viewPager.setPadding(offsetPx, 0, offsetPx, 0);

            viewPager.setAdapter(new SliderAdapter(images, viewPager));
            viewPager.invalidate();
            ImageButton button = binding.fullscreenButton;
            button.setOnClickListener(view -> {
                Log.i(TAG, "showPictures: listener on viewPager called");
//                Bitmap[] bitmaps = new Bitmap[images.size()];
//                String[] names = new String[images.size()];
                Bundle bundle = new Bundle();
//                for (int i = 0;i < images.size(); i++){
//                    bitmaps[i] = images.get(i).getImage();
//                    names[i] = images.get(i).getName();
//                }

                //TODO remplacer sauvegarde des photos par url en sauvegarde par noms et passer par DAO cloud storage pour récupérer photos+ metadata (pour ne pas recréer un nom)

                Log.i(TAG, "showPictures: putting serializable in bundle");
                bundle.putStringArrayList(EXTRA_PICTURES_NAMES,picturesNames);
                bundle.putStringArrayList(EXTRA_PICTURES_PATHS,picturesPaths);

                Intent intent = new Intent(getContext(), FullScreenPictureSlideActivity.class);
                intent.putExtras(bundle);
//                intent.putExtra(LocationDAO.PICTURES_URI_KEY,bitmaps);
                Log.i(TAG, "showPictures: starting intent");
                startActivity(intent);
            });

        //TODO : ajouter listener pour montrer images en grand écran


    }
    private void showLoadScreen(){
        binding.getRoot().setEnabled(false);
        binding.locationProgressBarContainer.setVisibility(View.VISIBLE);
    }
    private void hideLoadScreen(){
        binding.getRoot().setEnabled(true);
        binding.locationProgressBarContainer.setVisibility(View.GONE);
    }
    public void downloadPictures(){
        if (mapLocation.getPictures() != null) {
            showLoadScreen();
            PictureDownloader.getBitmapsFromURL(mapLocation,picturesNames,picturesPaths,getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),this);
        } else {
            Log.i(TAG, "downloadPictures: no pictures in mapLocation");
        }
    }
    public void getBitmapsFromURL() {

        new Thread(() -> {
            try {
                int i = 1;
                getActivity().runOnUiThread(this::showLoadScreen);
                for (String s : mapLocation.getPictures()) {

                    if (s != null) {
                        String pictureName;
                        String picTurePath;
                        URL url = new URL(s);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();

                        InputStream input = connection.getInputStream();

                        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), PictureDownloader.PICTURES_LOCAL_FOLDER);

                        // Create the storage directory if it does not exist
                        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()){
                            Log.d(TAG, "failed to create directory");
                        }
                        Log.i(TAG, "takePicture: file path is: "+ mediaStorageDir.getPath());
                        // Return the file target for the photo based on filename
                        pictureName = MapLocation.generatePictureName(mapLocation.getId(),i);
                        picTurePath = mediaStorageDir.getPath() + File.separator + pictureName;
                        File file = new File(picTurePath);
                        if(!file.exists()) {
                            Log.i(TAG, "getBitmapsFromURL: picture file does not exist");
                            file.createNewFile();
                            BufferedInputStream bufferedInputStream = new BufferedInputStream(input);
                            FileOutputStream fos = new FileOutputStream(file);
                            int in = 0;
                            Log.i(TAG, "getBitmapsFromURL: writing on file...");
                            while ((in = bufferedInputStream.read()) != -1){
                                fos.write(in);
                            }
                            
                        }
                        else{
                            Log.i(TAG, "getBitmapsFromURL: picture already exists");
                        }
                        picturesPaths.add(picTurePath);
                        picturesNames.add(pictureName);

                        Log.i(TAG, "getBitmapsFromURL: getting Bitmap from file");
                        Bitmap myBitmap = BitmapFactory.decodeFile(picTurePath);
                        myBitmap = ExifUtil.rotateBitmap(picTurePath,myBitmap);
                        Log.i(TAG, "getBitmapsFromURL: bitmap width : "+myBitmap.getWidth()+" height : "+myBitmap.getHeight());
                        images.add(new SliderItem(myBitmap,pictureName));
                        i++;
                    }
                }

                getActivity().runOnUiThread(() ->{
                    showPictures();
                    hideLoadScreen();
                });



            } catch (IOException e) {
                Toast.makeText(getContext(), getString(R.string.picture_retrieve_error), Toast.LENGTH_SHORT).show();

            }

        }).start();


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
}