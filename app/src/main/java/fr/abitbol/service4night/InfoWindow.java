package fr.abitbol.service4night;

import android.content.Context;
import android.util.AttributeSet;
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


public class InfoWindow extends GridLayout implements GoogleMap.InfoWindowAdapter {


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
            
        }
        return this;
    }
}
