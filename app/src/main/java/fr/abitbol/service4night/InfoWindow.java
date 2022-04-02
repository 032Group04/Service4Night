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



public class InfoWindow extends GridLayout implements GoogleMap.InfoWindowAdapter {

    private Context context;
    public InfoWindow(Context _context) {
        super(_context);
        context = _context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflate(_context,R.layout.window_info,this);
    }

    public InfoWindow(Context _context, @Nullable AttributeSet attrs) {
        super(_context, attrs);
        context = _context;
        inflate(_context,R.layout.window_info,this);
    }

    public InfoWindow(Context _context, AttributeSet attrs, int defStyleAttr) {
        super(_context, attrs, defStyleAttr);
        context = _context;
        inflate(_context,R.layout.window_info,this);
    }

    public InfoWindow(Context _context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(_context, attrs, defStyleAttr, defStyleRes);
        context = _context;
        inflate(_context,R.layout.window_info,this);
    }

    @Nullable
    @Override
    public View getInfoWindow(@NonNull Marker marker) {
        return null;
    }

    @Nullable
    @Override
    public View getInfoContents(@NonNull Marker marker) {
    return this;
    }
}
