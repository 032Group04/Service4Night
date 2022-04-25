package fr.abitbol.service4night;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.databinding.WindowInfoBinding;
import fr.abitbol.service4night.services.BathroomService;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class InfoWindow  implements GoogleMap.InfoWindowAdapter {

    private final String TAG = "InfoWindow logging";
    private WindowInfoBinding binding;
    private AtomicReference<ArrayList<Location>> locationsRef;
    private MapsActivity mapsActivity;
    //private View mContent;

    public InfoWindow(MapsActivity context){

        //LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //mContent = inflater.inflate(R.layout.window_info,null);

        mapsActivity =  context;
        binding = WindowInfoBinding.inflate(mapsActivity.getLayoutInflater());
        //mContent = mapsActivity.getLayoutInflater().inflate(R.layout.window_info,null);
    }

    private void init(){
        Log.i(TAG, "init() called");

//        binding.getRoot().setOnClickListener(view -> {
//            Log.i(TAG, "init: infoWindow clicked");
//        });


    }
    public CheckBox getCheckbox(int count){
        switch (count){
            case 0 : {
               break;
            }
            case 1 : return binding.checkbox11;

            case 2 : return binding.checkBox12;

            case 3 : return binding.checkBox13;

            case 4 : return binding.checkBox21;

            case 5 : return binding.checkBox22;

            case 6 : return binding.checkBox23;


        }
        return null;
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        Log.i(TAG, "getInfoWindow called");
        return null;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        locationsRef = new AtomicReference<>(mapsActivity.getVisibleLocations());
        Location location = null;
        if (locationsRef.get() != null) {
            for (Location l : locationsRef.get()){
                if (l.getId().equals(marker.getTitle()) ){
                    location = l;
                    break;
                }
            }    
        }
        else{
            Log.i(TAG, "getInfoContents: locationRef is null");
        }
        
        if (location != null){
            Log.i(TAG, "getInfoContents: location is not null");
            binding.descriptionTextView.setText(location.getDescription());
            Log.i(TAG, "getInfoContents: description is : "+ location.getDescription());
            binding.coordinatesTextView.setText(String.format(Locale.ENGLISH,"Lat : %f - Long : %f",location.getPoint().latitude,location.getPoint().longitude) );
            int count =0;
            if (location.getServices().containsKey(Service.ELECTRICITY_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of electricity service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.electricityLabel);
                box.setVisibility(View.VISIBLE);
                box.setChecked(true);
            }
            if (location.getServices().containsKey(Service.WATER_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of water service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.waterLabel);
                box.setVisibility(View.VISIBLE);
                box.setChecked(true);
                if (((WaterService) (location.getServices().get(Service.WATER_SERVICE))).isDrinkable()) {
                    count++;
                    CheckBox box2 = getCheckbox(count);
                    box2.setText(R.string.drinkable_label);
                    box2.setVisibility(View.VISIBLE);
                    box2.setChecked(true);


                }
            }
            if (location.getServices().containsKey(Service.INTERNET_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of internet service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.internet_label);
                box.setVisibility(View.VISIBLE);
                box.setChecked(true);

            }
            if (location.getServices().containsKey(Service.DUMPSTER_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of dump service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.dumpster_label);
                box.setVisibility(View.VISIBLE);
                box.setChecked(true);

            }
            if (location.getServices().containsKey(Service.DRAINAGE_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of drain service");
                count++;
                CheckBox box = getCheckbox(count);

                box.setVisibility(View.VISIBLE);

                box.setText((((DrainService) location.getServices().get(Service.DRAINAGE_SERVICE))
                        .isBlackWater())? R.string.black_water_drain_label : R.string.grey_water_drain_label);

                box.setChecked(true);

            }
//            location.getServices().forEach((s, service) -> {
//                System.out.println(s);
//            });
//            location.getServices().forEach((str, service) ->{
//                if (service != null) {
//                    Log.i(TAG, "getInfoContents: service is : " + str);
//                    if (service instanceof ElectricityService){
//                        Log.i(TAG, "getInfoContents:  instance of electricity service");
//                        binding.electricityCheckBox.setChecked(true);
//                    }
//                    else if (service instanceof WaterService){
//                        Log.i(TAG, "getInfoContents:  instance of water service");
//                        binding.waterCheckBox.setChecked(true);
//                        if (((WaterService) service).isDrinkable()) {
//                            binding.drinkableCheckBox.setVisibility(VISIBLE);
//                            binding.drinkableCheckBox.setChecked(true);
//
//                        }
//                    }
//                    else if (service instanceof InternetService){
//                        Log.i(TAG, "getInfoContents:  instance of internet service");
//                        binding.internetCheckBox.setChecked(true);
//
//                    }
//                    else if (service instanceof DumpService){
//                        Log.i(TAG, "getInfoContents:  instance of dump service");
//                        binding.dumpsterCheckBox.setChecked(true);
//
//                    }
//                    else if (service instanceof DrainService){
//                        Log.i(TAG, "getInfoContents:  instance of drain service");
//
//                        if (((DrainService) service).isBlackWater()){
//                            binding.drainCheckBox.setText(R.string.black_water_drain_label);
//                        }
//                        binding.drainCheckBox.setChecked(true);
//
//                    }
//
//                }
//            });
        }
        //binding.getRoot().invalidate();
        return binding.getRoot();
    }
}
