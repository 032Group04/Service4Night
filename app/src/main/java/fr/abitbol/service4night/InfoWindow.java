package fr.abitbol.service4night;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import fr.abitbol.service4night.databinding.WindowInfoBinding;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.WaterService;


public class InfoWindow extends GridLayout implements GoogleMap.InfoWindowAdapter {

    private final String TAG = "InfoWindow logging";
    private WindowInfoBinding binding;
    private AtomicReference<ArrayList<Location>> locationsRef;

    public InfoWindow(Context context) {
        super(context);
        init(context);

//        inflate(context,R.layout.window_info,this);
    }

    public InfoWindow(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
//        inflate(context,R.layout.window_info,this);
        init(context);
    }

    public InfoWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        inflate(context,R.layout.window_info,this);
        init(context);
    }

    public InfoWindow(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
//        inflate(context,R.layout.window_info,this);
    }
    private void init(Context context){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.window_info,this);
        binding = WindowInfoBinding.inflate(inflater);
        MapsActivity mapsActivity =  (MapsActivity) context;
        locationsRef = new AtomicReference<>(mapsActivity.getVisibleLocations());

    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
        Location location = null;

        for (Location l : locationsRef.get()){
            if (l.getId().equals(marker.getTitle()) ){
                location = l;
                break;
            }
        }
        if (location != null){
            binding.descriptionTextView.setText(location.getDescription());
            binding.coordinatesTextView.setText(location.getPoint().toString());
            location.getServices().forEach((str, service) ->{
                if (service != null) {
                    Log.i(TAG, "getInfoContents: service is : " + str);
                    if (service instanceof ElectricityService){
                        Log.i(TAG, "getInfoContents:  instance of electricity service");
                        binding.electricityCheckBox.setChecked(true);
                    }
                    else if (service instanceof WaterService){
                        Log.i(TAG, "getInfoContents:  instance of water service");
                        binding.waterCheckBox.setChecked(true);
                        if (((WaterService) service).isDrinkable()) {
                            binding.drinkableCheckBox.setVisibility(VISIBLE);
                            binding.drinkableCheckBox.setChecked(true);

                        }
                    }
                    else if (service instanceof InternetService){
                        Log.i(TAG, "getInfoContents:  instance of internet service");

                    }
                    else if (service instanceof DumpService){
                        Log.i(TAG, "getInfoContents:  instance of dump service");

                    }
                    else if (service instanceof DrainService){
                        Log.i(TAG, "getInfoContents:  instance of drain service");

                    }
                }
            });
        }
        return this;
    }
}
