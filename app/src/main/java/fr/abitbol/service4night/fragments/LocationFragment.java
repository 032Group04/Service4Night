/*
 * Nom de classe : LocationFragment
 *
 * Description   : affichage des lieux
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.fragments;

import android.content.Intent;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

import fr.abitbol.service4night.databinding.FragmentLocationBinding;
import fr.abitbol.service4night.pictures.FullScreenPictureSlideActivity;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.locations.MapLocation;

import fr.abitbol.service4night.R;
import fr.abitbol.service4night.listeners.OnPictureDownloadListener;
import fr.abitbol.service4night.pictures.PictureDownloader;
import fr.abitbol.service4night.pictures.SliderAdapter;
import fr.abitbol.service4night.pictures.SliderItem;
import fr.abitbol.service4night.locations.DrainService;
import fr.abitbol.service4night.locations.ElectricityService;
import fr.abitbol.service4night.locations.InternetService;
import fr.abitbol.service4night.locations.Service;
import fr.abitbol.service4night.locations.WaterService;


public class LocationFragment extends Fragment implements OnPictureDownloadListener {


    private static final String TAG = "LocationFragment logging";
    public static final String EXTRA_PICTURES_PATHS = "picturesPaths";
    public static final String EXTRA_PICTURES_NAMES = "picturesNames";
    public static final String IMAGES_NAME = "images";
    public static final String MAPLOCATION_NAME = "mapLocation";
    public static final String POINT_NAME = "point";
    public static final String PICTURE_PATH_NAME = "picturePath";
    public static final String PICTURE_URI_NAME = "userPictureUri";
    private  static final String ILLUSTRATION_PICTURE_NAME = "no_picture";
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


        if (images != null && images.size() > 0){
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
                    //TODO si temps : gérer problème images
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
                    mapLocation = ((MapLocation) getArguments().getParcelable(MapsFragment.MAPLOCATION_EXTRA_NAME));
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
            Log.i(TAG, "onCreateView: mainActivity is not null, hiding settings");
            mainActivity.setSettingsVisibility(false);
            int metricsDensity = getResources().getDisplayMetrics().densityDpi;
            if (metricsDensity == DisplayMetrics.DENSITY_HIGH){
                Log.i(TAG, "onCreateView: density is high");
                mainActivity.setTitle(mapLocation.getName());
                mainActivity.setActionBarVisible(true);
            }
            else {
                Log.i(TAG, "onCreateView: density is medium or low");
                int orientation = mainActivity.getWindowManager().getDefaultDisplay().getRotation();
                if (orientation == Surface.ROTATION_0 || orientation == Surface.ROTATION_180) {
                    Log.i(TAG, "onCreateView: screen in default rotation, showing actionBar ");
                    mainActivity.setTitle(mapLocation.getName());
                    mainActivity.setActionBarVisible(true);
                } else if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
                    Log.i(TAG, "onCreateView: screen is in secondary rotation, hiding actionBar");
                    mainActivity.setActionBarVisible(false);
                }
            }
            Log.i(TAG, "onViewCreated: main activity not null, setting title");
        }
        else{
            Log.i(TAG, "onCreateView: mainActivity is null");

        }


        Log.i(TAG, "getInfoContents: mapLocation is not null");
        binding.locationDescriptionEditText.setText(mapLocation.getDescription());
        Log.i(TAG, "getInfoContents: description is : " + mapLocation.getDescription());
        binding.locationCoordinatesTextView.setText(String.format(Locale.ENGLISH, "Lat : %f - Long : %f", mapLocation.getPoint().latitude, mapLocation.getPoint().longitude));

        /*
         * affichage des images si elles ont déja été chargées
         */
        if (!images.isEmpty() ){
            showPictures();
        }
        else {
            /*
             * téléchargement des images
             */
            if (mapLocation.getPictures() != null && !mapLocation.getPictures().isEmpty()) {
                downloadPictures();
            }
            /*
             * le lieu n'a pas d'images
             */
            else{
                Log.i(TAG, "onCreateView: location without pictures");
                binding.fullscreenButton.setVisibility(View.GONE);
                images.add(new SliderItem(BitmapFactory.decodeResource(getResources(),R.drawable.no_picture),ILLUSTRATION_PICTURE_NAME));
                showPictures();

            }
        }
        showServices();



        return binding.getRoot();
    }
    //TODO si temps: bouton street view

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

    /*
     * affichage des services présents
     */

    private void showServices() {
        Map<String, Service> services = mapLocation.getServices();

        // eau
        if (services.containsKey(Service.WATER_SERVICE)) {
            WaterService waterService = (WaterService) services.get(Service.WATER_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of water service");

            CheckBox[] box = getCheckBoxesPosition(3);

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
        // électricité
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
        // poubelles
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
        // accès internet
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
        // vidange eaux sales
        if (services.containsKey(Service.DRAINAGE_SERVICE)) {
            Service drainService = services.get(Service.DRAINAGE_SERVICE);
            Log.i(TAG, "getInfoContents:  instance of drain service");
            CheckBox box = getCheckBoxesPosition(1)[0];
            box.setText((((DrainService) drainService).isBlackWater()) ? R.string.black_water_drain_label : R.string.grey_water_drain_label);
            box.setButtonDrawable(R.drawable.ic_service_drainage);
            box.setVisibility(View.VISIBLE);
        }
    }


    /*
     * renvoie l'emplacement d'affichage de service correspondant à l'index passé en paramètre
     */
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
    /*
     * attribue les emplacements d'affichage de service selon le nombre de cases nécessaires
     */
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

    /*
     * charge les photos dans le viewPager
     */
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
                Bundle bundle = new Bundle();

                //TODO si temps : remplacer sauvegarde des photos par url en sauvegarde par noms et passer par DAO cloud storage pour récupérer photos+ metadata (pour ne pas recréer un nom)

                Log.i(TAG, "showPictures: putting serializable in bundle");
                bundle.putStringArrayList(EXTRA_PICTURES_NAMES,picturesNames);
                bundle.putStringArrayList(EXTRA_PICTURES_PATHS,picturesPaths);

                Intent intent = new Intent(getContext(), FullScreenPictureSlideActivity.class);
                intent.putExtras(bundle);
                Log.i(TAG, "showPictures: starting intent");
                startActivity(intent);
            });



    }
    /*
     * affiche/cache l'écran de chargement en cours
     */

    private void showLoadScreen(){
        binding.getRoot().setEnabled(false);
        binding.locationProgressBarContainer.setVisibility(View.VISIBLE);
    }
    private void hideLoadScreen(){
        binding.getRoot().setEnabled(true);
        binding.locationProgressBarContainer.setVisibility(View.GONE);
    }
    /*
     * télécharge les photos à partir des url présentes dans le lieu
     */
    public void downloadPictures(){
        if (mapLocation.getPictures() != null) {
            showLoadScreen();
            PictureDownloader.getBitmapsFromURL(mapLocation,picturesNames,picturesPaths,getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),this);
        } else {
            Log.i(TAG, "downloadPictures: no pictures in mapLocation");
        }
    }

    /*
     * callback sur le téléchargement des photos
     */
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