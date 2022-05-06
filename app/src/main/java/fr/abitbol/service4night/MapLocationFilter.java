package fr.abitbol.service4night;

import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Map;

import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.services.Service;

public class MapLocationFilter {
    private static final String TAG = "MapLocationFilter logging";


    public static List<MapLocation> filterByLocation(LatLngBounds latLngBounds, List<MapLocation> mapLocations){
        Log.i(TAG, "before filtering arraylist size is: " + mapLocations.size());
        mapLocations.removeIf(location -> !(latLngBounds.contains(location.getPoint())));
        Log.i(TAG, "after filtering arraylist size is: " + mapLocations.size());
        return mapLocations;

    }
    public static List<MapLocation> filterByServices(Map<String, Service> requiredServices, List<MapLocation> mapLocations){
        Log.i(TAG, "filterByServices called");
        mapLocations.removeIf(mapLocation -> {
            for (Service s : requiredServices.values()){
                if(!(mapLocation.hasService(s.getLabel()))){

                    return true;
                }
                else if (!(mapLocation.getServices().get(s.getLabel()).matchFilter(s))){
                    return true;
                }
            }
            return false;
        });
        return mapLocations;
    }

}
