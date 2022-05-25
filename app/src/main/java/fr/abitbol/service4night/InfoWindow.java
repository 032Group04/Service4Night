package fr.abitbol.service4night;

import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.databinding.WindowInfoBinding;
import fr.abitbol.service4night.fragments.MapsFragment;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;


public class InfoWindow  implements GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private final String TAG = "InfoWindow logging";
    private WindowInfoBinding binding;
    private AtomicReference<ArrayList<MapLocation>> locationsRef;
    private MapLocation mapLocation;
    private MapsFragment mapsActivity;
    //private View mContent;

    public InfoWindow(MapsFragment context){
        Log.i(TAG, "InfoWindow: constructor called");
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
        Log.i(TAG, "getCheckbox: getting box number "+count);
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
    public void resetCheckBoxes(){
        for (int i = 1;i< 7;i++){
            getCheckbox(i).setVisibility(View.INVISIBLE);
            getCheckbox(i).setText("");
            getCheckbox(i).setButtonDrawable(android.R.drawable.checkbox_off_background);
        }
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
        Log.i(TAG, "getInfoContents called");
        resetCheckBoxes();
        for (int i = 0; i<= 6 ; i++){
            if (getCheckbox(i) != null){
                getCheckbox(i).setVisibility(View.INVISIBLE);
            }
        }
        locationsRef = new AtomicReference<>(mapsActivity.getVisibleLocations());

        if (locationsRef.get() != null) {
            for (MapLocation l : locationsRef.get()){
                if (l.getId().equals(marker.getTitle()) ){
                    mapLocation = l;
                    break;
                }
            }    
        }
        else{
            Log.i(TAG, "getInfoContents: locationRef is null");
        }

        if (mapLocation != null){

            Log.i(TAG, "getInfoContents: mapLocation is not null");
            binding.nameTextView.setText(mapLocation.getName());
            binding.descriptionTextView.setText(mapLocation.getDescription());
            Log.i(TAG, "getInfoContents: description is : "+ mapLocation.getDescription());
            binding.coordinatesTextView.setText(String.format(Locale.ENGLISH,"Lat : %f - Long : %f", mapLocation.getPoint().latitude, mapLocation.getPoint().longitude) );
            int count =0;
            Map<String, Service> services = mapLocation.getServices();

            if (services.containsKey(Service.ELECTRICITY_SERVICE)){
                ElectricityService electricityService = (ElectricityService) services.get(Service.ELECTRICITY_SERVICE);
                Log.i(TAG, "getInfoContents:  instance of electricity service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.electricityLabel);
                box.setButtonDrawable(R.drawable.ic_service_electricity);
                box.setVisibility(View.VISIBLE);
                count++;
                CheckBox box2 = getCheckbox(count);
                if (electricityService.getPrice() == 0){
                    box2.setButtonDrawable(R.drawable.ic_service_free);
                    box2.setText(R.string.free_electricity);
                    box2.setVisibility(View.VISIBLE);
                }

            }
            if (services.containsKey(Service.WATER_SERVICE)){
                Service waterService = services.get(Service.WATER_SERVICE);
                Log.i(TAG, "getInfoContents:  instance of water service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setButtonDrawable(R.drawable.ic_service_water);
                box.setText(R.string.waterLabel);
                box.setVisibility(View.VISIBLE);


                if (((WaterService)waterService).isDrinkable()) {
                    count++;
                    CheckBox box2 = getCheckbox(count);
                    box2.setButtonDrawable(R.drawable.ic_service_drinkable_water);
                    box2.setText(R.string.drinkable_label);
                    box2.setVisibility(View.VISIBLE);

                }
//                else {
//                    box2.setButtonDrawable(R.drawable.ic_service_not_drinkable_water);
//                    box2.setText(R.string.not_drinkable_label);
//                    box2.setVisibility(View.VISIBLE);
//                }
            }



            if (services.containsKey(Service.INTERNET_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of internet service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.internet_label);
                box.setButtonDrawable(R.drawable.ic_service_internet);
                box.setVisibility(View.VISIBLE);

            }
            if (services.containsKey(Service.DUMPSTER_SERVICE)){
                Log.i(TAG, "getInfoContents:  instance of dump service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText(R.string.dumpster_label);
                box.setButtonDrawable(R.drawable.ic_service_dumpster);
                box.setVisibility(View.VISIBLE);

            }
            if (services.containsKey(Service.DRAINAGE_SERVICE)){
                Service drainService = services.get(Service.DRAINAGE_SERVICE);
                Log.i(TAG, "getInfoContents:  instance of drain service");
                count++;
                CheckBox box = getCheckbox(count);
                box.setText((((DrainService)drainService).isBlackWater())? R.string.black_water_drain_label : R.string.grey_water_drain_label);
                box.setButtonDrawable(R.drawable.ic_service_drainage);
                box.setVisibility(View.VISIBLE);
            }
            binding.getRoot().invalidate();

        }

        return binding.getRoot();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.i(TAG, "finalize called");

    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        if (mapsActivity!= null && mapLocation != null){
            mapsActivity.infoWindowClicked(mapLocation);
            Log.i(TAG, "getInfoContents: listener on infowindow clicked");
        }
        else{
            Log.i(TAG, "onInfoWindowClick: mapsActivity or MapLocation is null");
        }
    }
}
