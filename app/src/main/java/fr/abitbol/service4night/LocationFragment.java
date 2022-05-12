package fr.abitbol.service4night;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.DAO.LocationDAO;
import fr.abitbol.service4night.databinding.FragmentLocationBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationFragment extends Fragment {


    private static final String TAG = "LocationFragment logging";
    public static final String EXTRA_PICTURES_PATHS = "picturesPaths";
    public static final String EXTRA_PICTURES_NAMES = "picturesNames";
    private FragmentLocationBinding binding;
    private LatLng point;
    private String name;
    private MapLocation mapLocation;
    private List<SliderItem> images;
    boolean[][] boxGrid;
    private ViewPager2 viewPager;
    private ArrayList<String> picturesPaths;
    private ArrayList<String> picturesNames;

    public LocationFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        images = new ArrayList<>();
        boxGrid = new boolean[3][3];
        for (int y = 0;y < 3;y++){
            for (int x = 0; x < 3; x++){
                boxGrid[x][y] = true;
            }
        }
        Log.i(TAG, "onCreate called ");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLocationBinding.inflate(inflater,container,false);
        if (savedInstanceState == null) {
            if (getArguments() != null) {
                try {
                    mapLocation = ((MapLocation) getArguments().getParcelable(MapsActivity.MAPLOCATION_EXTRA_NAME));
                    if (mapLocation != null) {
                        Log.i(TAG, "onCreateView: maplocation parcelable is not null");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            picturesNames = null;
            picturesPaths = null;
            if (mapLocation != null) {

                /*
                 * modification du titre
                 */
                MainActivity mainActivity = (MainActivity) getActivity();
                if (mainActivity != null){
                    Log.i(TAG, "onViewCreated: main activity not null");
                    ActionBar actionBar = mainActivity.getSupportActionBar();
                    if (actionBar != null){
                        Log.i(TAG, "onViewCreated: action bar not null");
                        actionBar.setTitle(mapLocation.getName());

                    }
                    else{ Log.i(TAG, "onViewCreated: action bar is null");}

                    Log.i(TAG, "onCreateView: mainActivity is not null, hiding settings");
                    mainActivity.setSettingsVisibility(false);

                }
                else Log.i(TAG, "onViewCreated: mainActivity is null");
                Log.i(TAG, "getInfoContents: mapLocation is not null");
                binding.locationNameTextView.setText(mapLocation.getName());
                binding.locationDescriptionEditText.setText(mapLocation.getDescription());
                Log.i(TAG, "getInfoContents: description is : " + mapLocation.getDescription());
                binding.locationCoordinatesTextView.setText(String.format(Locale.ENGLISH, "Lat : %f - Long : %f", mapLocation.getPoint().latitude, mapLocation.getPoint().longitude));

                Map<String, Service> services = mapLocation.getServices();

                if (services.containsKey(Service.WATER_SERVICE)) {
                    WaterService waterService = (WaterService) services.get(Service.WATER_SERVICE);
                    Log.i(TAG, "getInfoContents:  instance of water service");

                    CheckBox[] box = getPosition(3);
                    for (CheckBox c : box) {
                        if (c == null) {
                            Log.i(TAG, "onCreateView: checkbox is null");
                            if (binding.locationCheckBox11 == null) {
                                Log.i(TAG, "onCreateView: checkBox is null at source");
                            }
                        }
                    }
                    box[0].setButtonDrawable(R.drawable.ic_service_water);
                    box[0].setText(R.string.waterLabel);
                    box[0].setVisibility(View.VISIBLE);

                    Log.i(TAG, "onCreateView: setting drinkable");
                    if (waterService.isDrinkable()) {

                        box[1].setButtonDrawable(R.drawable.ic_service_drinkable_water);
                        box[1].setText(R.string.drinkable_label);
                        box[1].setVisibility(View.VISIBLE);

                    } else {
                        box[1].setButtonDrawable(R.drawable.ic_service_not_drinkable_water);
                        box[1].setText(R.string.not_drinkable_label);
                        box[1].setVisibility(View.VISIBLE);
                    }
                    Log.i(TAG, "onCreateView: setting price");
                    if (waterService.getPrice() == 0) {

                        box[2].setButtonDrawable(R.drawable.ic_service_free);
                        box[2].setText(getString(R.string.free_water));

                    } else {
                        box[2].setButtonDrawable(R.drawable.ic_service_paying);
                        box[2].setText(String.valueOf(waterService.getPrice()) + " €");
                    }
                    box[2].setVisibility(View.VISIBLE);
                }

                if (services.containsKey(Service.ELECTRICITY_SERVICE)) {
                    ElectricityService electricityService = (ElectricityService) services.get(Service.ELECTRICITY_SERVICE);
                    Log.i(TAG, "getInfoContents:  instance of electricity service");

                    CheckBox[] box = getPosition(2);
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

                }
                if (services.containsKey(Service.DUMPSTER_SERVICE)) {
                    Log.i(TAG, "getInfoContents:  instance of dump service");

                    CheckBox box = getPosition(1)[0];
                    box.setText(R.string.dumpster_label);
                    box.setButtonDrawable(R.drawable.ic_service_dumpster);
                    box.setVisibility(View.VISIBLE);

                }

                if (services.containsKey(Service.INTERNET_SERVICE)) {
                    InternetService internetService = (InternetService) services.get(Service.INTERNET_SERVICE);
                    Log.i(TAG, "getInfoContents:  instance of internet service");

                    CheckBox[] box = getPosition(2);

                    if (internetService.getConnectionType().equals(InternetService.ConnectionType.public_wifi)) {

                        box[0].setButtonDrawable(R.drawable.ic_service_wifi);
                        box[0].setText(R.string.wifi_label);
                        box[0].setVisibility(View.VISIBLE);

                    } else {
                        box[0].setText(R.string.internet_label);
                        box[0].setButtonDrawable(R.drawable.ic_service_internet);
                        box[0].setVisibility(View.VISIBLE);
                    }

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

                if (services.containsKey(Service.DRAINAGE_SERVICE)) {
                    Service drainService = services.get(Service.DRAINAGE_SERVICE);
                    Log.i(TAG, "getInfoContents:  instance of drain service");
                    CheckBox box = getPosition(1)[0];
                    box.setText((((DrainService) drainService).isBlackWater()) ? R.string.black_water_drain_label : R.string.grey_water_drain_label);
                    box.setButtonDrawable(R.drawable.ic_service_drainage);
                    box.setVisibility(View.VISIBLE);
                }
                if (mapLocation.getPictures() != null && !mapLocation.getPictures().isEmpty()) {

                    picturesNames = new ArrayList<>();
                    picturesPaths = new ArrayList<>();
                    viewPager = binding.locationViewPager;
                    //TODO: transormer List<Uri> en Map et ajouter metadata nom picture
                    getBitmapsFromURL();


                }


            }
        }
        return binding.getRoot();
    }
    //TODO: bouton street view

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState ==null) {
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

    private CheckBox[] getPosition(int groupSize) {
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
                //TODO rendre SliderItem parcelable
//                for (int i = 0;i < images.size(); i++){
//                    bitmaps[i] = images.get(i).getImage();
//                    names[i] = images.get(i).getName();
//                }
                //TODO rendre SliderItem parcelable
                //TODO changer code une fois que les noms des photos seront dans mapLocation

                Log.i(TAG, "showPictures: putting serializable in bundle");
                bundle.putStringArrayList(EXTRA_PICTURES_NAMES,picturesNames);
                bundle.putStringArrayList(EXTRA_PICTURES_PATHS,picturesPaths);
                Intent intent = new Intent(getContext(),FullScreenPictureSlideActivity.class);
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

                        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Locations pictures");

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
}