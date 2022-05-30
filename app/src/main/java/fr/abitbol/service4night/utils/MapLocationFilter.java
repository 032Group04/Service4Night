/*
 * Nom de classe : MapLocationFilter
 *
 * Description   : contient des méthodes de filtrage pour les lieux
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.utils;

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

    /*
    *   filtre les lieux présents dans la zone passée en paramètre (latLngBounds)
    */
    public static ArrayList<MapLocation> filterByMapBounds(LatLngBounds latLngBounds, ArrayList<MapLocation> mapLocations){
        Log.i(TAG, "before filtering arraylist size is: " + mapLocations.size());
        mapLocations.removeIf(location -> !(latLngBounds.contains(location.getPoint())));
        Log.i(TAG, "after filtering arraylist size is: " + mapLocations.size());
        return mapLocations;

    }
    /*
     * filtre selon les services disponibles
     */
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
    /*
     * filtre selon l'utilisateur ayant ajouté le lieu
     */
    public static List<MapLocation> filterByUser(String userId, List<MapLocation> mapLocations){
        mapLocations.removeIf(mapLocation -> !(mapLocation.getUser_id().equals(userId)));
        return mapLocations;
    }
    /*
     * filtre utilisant le prédicat passé en paramètre
     */
    public static List<MapLocation> filterByPredicate(Predicate<MapLocation> predicate, List<MapLocation> mapLocations){
        ArrayList<MapLocation> copy = new ArrayList<>();
        mapLocations.stream().filter(predicate).forEach(copy::add);

        return copy;
    }

}
