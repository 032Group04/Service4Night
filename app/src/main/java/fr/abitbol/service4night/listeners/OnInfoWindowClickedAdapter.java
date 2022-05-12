package fr.abitbol.service4night.listeners;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import fr.abitbol.service4night.MapLocation;

public interface OnInfoWindowClickedAdapter {
    public abstract void infoWindowClicked(MapLocation location);


}
