package fr.abitbol.service4night;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.databinding.FragmentLocationBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class LocationFragment extends Fragment {


    private static final String TAG = "LocationFragment logging";
    private FragmentLocationBinding binding;
    private LatLng point;
    private String name;
    private MapLocation mapLocation;
    private List<SliderItem> images;
    boolean[][] boxGrid;
    private ViewPager2 viewPager;

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
            if (mapLocation != null) {
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

    public void showPictures(){


            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(2);
            int offsetPx = Math.round(getResources().getDisplayMetrics().density * 20);

            viewPager.setPadding(offsetPx, 0, offsetPx, 0);

            viewPager.setAdapter(new SliderAdapter(images, viewPager));
            viewPager.invalidate();

        //TODO : ajouter listener pour montrer images en grand écran


    }
    public void getBitmapsFromURL() {

        new Thread(() -> {
            try {
                int i = 1;
                for (String s : mapLocation.getPictures()) {

                    if (s != null) {
                        URL url = new URL(s);
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        Bitmap myBitmap = BitmapFactory.decodeStream(input);
                        images.add(new SliderItem(myBitmap,MapLocation.generatePictureName(mapLocation.getId(),i)));
                        i++;
                    }
                }
                getActivity().runOnUiThread(this::showPictures);



            } catch (IOException e) {
                Toast.makeText(getContext(), getString(R.string.picture_retrieve_error), Toast.LENGTH_SHORT).show();

            }

        }).start();


    }
}