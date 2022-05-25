package fr.abitbol.service4night;

import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.services.Service;

public class MapLocationFilter {
    private static final String TAG = "MapLocationFilter logging";


    public static ArrayList<MapLocation> filterByMapBounds(LatLngBounds latLngBounds, ArrayList<MapLocation> mapLocations){
        Log.i(TAG, "before filtering arraylist size is: " + mapLocations.size());
        mapLocations.removeIf(location -> !(latLngBounds.contains(location.getPoint())));
        Log.i(TAG, "after filtering arraylist size is: " + mapLocations.size());
        return mapLocations;

    }
    public static ArrayList<MapLocation> filterByServices(Map<String, Service> requiredServices, ArrayList<MapLocation> mapLocations){
        Log.i(TAG, "filterByServices called");
        ArrayList<MapLocation> copy = new ArrayList<>();
        mapLocations.stream().filter(mapLocation -> {
            for (Service s : requiredServices.values()){
                if(!(mapLocation.hasService(s.getLabel()))){

                    return false;
                }
                else if (!(mapLocation.getServices().get(s.getLabel()).matchFilter(s))){
                    return false;
                }
            }
            return true;
        }).forEach(copy::add);

        return copy;
    }
    public static List<MapLocation> filterByUser(String userId, List<MapLocation> mapLocations){
        mapLocations.removeIf(mapLocation -> !(mapLocation.getUser_id().equals(userId)));
        return mapLocations;
    }
    public static List<MapLocation> filterByPredicate(Predicate<MapLocation> predicate, List<MapLocation> mapLocations){
        ArrayList<MapLocation> copy = new ArrayList<>();
        mapLocations.stream().filter(predicate).forEach(copy::add);

        return copy;
    }

}
