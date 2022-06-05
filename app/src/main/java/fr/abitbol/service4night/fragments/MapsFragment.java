/*
 * Nom de classe : mapsFragment
 *
 * Description   : fragment affichant la carte Google maps.
 *
 * Auteur       : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.abitbol.service4night.DAO.DatabaseService;
import fr.abitbol.service4night.InfoWindow;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.locations.MapLocation;
import fr.abitbol.service4night.locations.MapLocationFilter;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.DrawerFilterBinding;
import fr.abitbol.service4night.databinding.FragmentMapsBinding;
import fr.abitbol.service4night.listeners.OnCompleteLocalisationListener;
import fr.abitbol.service4night.listeners.OnInfoWindowClickedAdapter;
import fr.abitbol.service4night.locations.DrainService;
import fr.abitbol.service4night.locations.DumpService;
import fr.abitbol.service4night.locations.ElectricityService;
import fr.abitbol.service4night.locations.InternetService;
import fr.abitbol.service4night.locations.Service;
import fr.abitbol.service4night.locations.WaterService;
import fr.abitbol.service4night.utils.UserLocalisation;

public class MapsFragment extends Fragment implements OnMapReadyCallback, OnCompleteListener<QuerySnapshot>, OnCompleteLocalisationListener, OnInfoWindowClickedAdapter {
    private GoogleMap mMap;
    private FragmentMapsBinding binding;
    private DrawerFilterBinding drawerBinding;
    private Location userLocation;
    private ArrayList<MapLocation> locations;
    private Map<String, Service> servicesFilters;
    private LatLng trimmedLatLng;
    private final String TAG = "mapsActivity logging";
    private String theme;
    private static final String VISIBLE_LOCATIONS_NAME = "visibleLocations";
    private static final String ADDING_NAME = "adding";
    private static final String SERVICE_FILTERS_NAME = "serviceFilters";
    private static final String USER_LOCATION_NAME = "userLocation";
    private static final String INITIAL_LOCATIONS_COUNT_NAME = "initialLocationsCount";
    private Integer initialLocationsCount;
    public static final int MAP_TYPE_EXPLORE = 3737;
    public static final int MAP_TYPE_ADD = 7337;
    public static final String MAP_MODE_BUNDLE_NAME = "mapMode";
    public static final String MAP_POINT_EXTRA_NAME = "point";
    public static final String ACTION_GET_POINT_LATLNG = "point_data";
    public static final String MAPLOCATION_EXTRA_NAME = "mapLocation";
    private Marker lastInfoWindowMarker;
    boolean adding;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        //TODO gérer orientation après avoir choisi lieu a ajouter
        super.onSaveInstanceState(outState);
        outState.putBoolean(ADDING_NAME,adding);
        if (userLocation != null)
            outState.putParcelable(USER_LOCATION_NAME,userLocation);

        if (adding){
            Log.i(TAG, "onSaveInstanceState: map is in add mode");
        }
        else{
            Log.i(TAG, "onSaveInstanceState: map is in explore mode");
            if (servicesFilters != null){
                ArrayList<Service> filtersList =  new ArrayList<>(servicesFilters.values());
                outState.putParcelableArrayList(SERVICE_FILTERS_NAME,filtersList);
            }
            if (initialLocationsCount != null){
                outState.putInt(INITIAL_LOCATIONS_COUNT_NAME,initialLocationsCount);
            }

            if (locations != null)
                outState.putParcelableArrayList(VISIBLE_LOCATIONS_NAME, locations);
        }

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreate called");

        binding = FragmentMapsBinding.inflate(inflater,container,false);

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null){
            Log.i(TAG, "onViewCreated: main activity not null");
//            ActionBar actionBar = mainActivity.getSupportActionBar();
//            if (actionBar != null){
//                Log.i(TAG, "onViewCreated: action bar not null");
//                actionBar.hide();
//
//            }
//            else{
//                Log.i(TAG, "onViewCreated: action bar is null");
//            }
//
//            Log.i(TAG, "onCreateView: mainActivity is not null, hiding settings");
//            mainActivity.setSettingsVisibility(false);
            mainActivity.setActionBarVisible(false);

        }
        else Log.i(TAG, "onViewCreated: mainActivity is null");
        // applique le thème jour/nuit
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        theme = preferences.getString(MainActivity.PREFERENCE_THEME_KEY,MainActivity.PREFERENCE_THEME_DEFAULT);
        if(theme.equals(getString(R.string.theme_light))){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (theme.equals(getString(R.string.theme_dark))){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{ Log.i(TAG,"theme preference unknown:" + theme);}

        Log.i(TAG, "onCreate: starting localisation");
        initialLocationsCount = null;
        locations = null;
        lastInfoWindowMarker = null;

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ADDING_NAME)) {
                adding = savedInstanceState.getBoolean(ADDING_NAME);
            }
            else{
                Toast.makeText(getContext(), getString(R.string.mapsActivity_open_mode_missing), Toast.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
            if (savedInstanceState.containsKey(USER_LOCATION_NAME)) {
                userLocation = savedInstanceState.getParcelable(USER_LOCATION_NAME);
            }
            if (!adding) {
                if (savedInstanceState.containsKey(VISIBLE_LOCATIONS_NAME)) {
                    locations = savedInstanceState.getParcelableArrayList(VISIBLE_LOCATIONS_NAME);
                    Log.i(TAG, "onCreate: visible locations are in savedInstanceState : "+ locations.size() + " locations" );

                }
                if (savedInstanceState.containsKey(INITIAL_LOCATIONS_COUNT_NAME)){
                    initialLocationsCount = savedInstanceState.getInt(INITIAL_LOCATIONS_COUNT_NAME);
                }

                if (savedInstanceState.containsKey(SERVICE_FILTERS_NAME)){
                    ArrayList<Service> filtersList = savedInstanceState.getParcelableArrayList(SERVICE_FILTERS_NAME);
                    servicesFilters = new HashMap<>();
                    filtersList.forEach(service -> {
                        servicesFilters.put(service.getLabel(),service);
                        Log.i(TAG, "onCreate: retrieving service filter : "+service.getLabel());
                    });
                    Log.i(TAG, "onCreate: service filters are available : " + servicesFilters.size()+ " filters");
                }
            }


        } else {
            if (getArguments() != null ) {


                Log.i(TAG, "onCreate: intent is valid and contains opening mode value");
                // récupère le type de carte exploration/ajout de lieu
                switch (getArguments().getInt(MAP_MODE_BUNDLE_NAME,-1)){
                    case MAP_TYPE_ADD:
                        adding = true;
                        binding.mapActionButton.setImageResource(R.drawable.ic_done);
                        break;
                    case  MAP_TYPE_EXPLORE:
                        adding = false;

                        break;
                    default:
                        // pas de mode d'ouverture : retour au menu principal
                        Log.i(TAG, "onCreate: no extra in map intent");
                        Toast.makeText(getContext(), getString(R.string.mapsActivity_open_mode_missing), Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(MapsFragment.this).popBackStack();


                }
            } else {
                Log.i(TAG, "onCreate: intent is null or doesn't contains opening mode value");

            }
        }


        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // affichage du bouton flottant selon le mode d'ouverture
        if (adding){
            binding.mapActionButton.setImageResource(R.drawable.ic_done);

        }
        else {

            // ajoute le Drawer de filtrage des lieux
            binding.mapActionButton.setImageResource(R.drawable.ic_search);
            drawerBinding = DrawerFilterBinding.inflate(getLayoutInflater());
            binding.getRoot().addView(drawerBinding.getRoot());


            /* locks the map while the drawer is open */

            drawerBinding.drawer.setOnDrawerOpenListener(() -> {
                Log.i(TAG, "onCreate: drawer opened");
                drawerBinding.getRoot().setClickable(true);

                listenFilters();
            });
            drawerBinding.drawer.setOnDrawerCloseListener(() -> {
                drawerBinding.getRoot().setClickable(false);
                if (!servicesFilters.isEmpty()) {
                    Log.i(TAG, "onCreate: some filters are selected ");

                } else {
                    Log.i(TAG, "onCreate: serviceFilters is empty ");
                }
                if (locations != null) {
                    showFilteredLocations(locations);
                }

            });


        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // localisation de l'utilisateur
        if (userLocation == null){
            new UserLocalisation(getContext()).locateUser(this);
        }


        binding.mapActionButton.show();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume called");
        super.onResume();


    }
    // écoute la selection de filtres
    public void listenFilters(){
        servicesFilters = new HashMap();
        drawerBinding.drawerElectricityCheckBox.setOnClickListener(view -> {
            if (((CheckBox)view).isChecked()){
                servicesFilters.put(Service.ELECTRICITY_SERVICE,new ElectricityService());
            }
            else {
                servicesFilters.remove(Service.ELECTRICITY_SERVICE);
            }
        });
        drawerBinding.drawerWaterCheckbox.setOnClickListener(view -> {
            if (((CheckBox)view).isChecked()){

                drawerBinding.drawerDrinkableCheckBox.setVisibility(View.VISIBLE);
                drawerBinding.drawerDrinkableCheckBox.setOnClickListener(v -> {
                    servicesFilters.put(Service.WATER_SERVICE, new WaterService((drawerBinding.drawerDrinkableCheckBox.isChecked())));

                });

                servicesFilters.put(Service.WATER_SERVICE, new WaterService((drawerBinding.drawerDrinkableCheckBox.isChecked())));

            }
            else {
                drawerBinding.drawerDrinkableCheckBox.setVisibility(View.INVISIBLE);

                servicesFilters.remove(Service.WATER_SERVICE);
            }
        });
        drawerBinding.drawerInternetCheckBox.setOnClickListener(view -> {
            if (((CheckBox)view).isChecked()){
                servicesFilters.put(Service.INTERNET_SERVICE,new InternetService(InternetService.ConnectionType.public_wifi));
            }
            else{
                servicesFilters.remove(Service.INTERNET_SERVICE);
            }
        });
        drawerBinding.drawerDrainCheckBox.setOnClickListener(view -> {
            if (((CheckBox)view).isChecked()){

                drawerBinding.drawerBlackWaterCheckBox.setVisibility(View.VISIBLE);
                drawerBinding.drawerBlackWaterCheckBox.setOnClickListener(v -> {
                    servicesFilters.put(Service.DRAINAGE_SERVICE, new DrainService((drawerBinding.drawerBlackWaterCheckBox.isChecked())));

                });

                servicesFilters.put(Service.DRAINAGE_SERVICE, new DrainService((drawerBinding.drawerBlackWaterCheckBox.isChecked())));

            }
            else {
                drawerBinding.drawerBlackWaterCheckBox.setVisibility(View.INVISIBLE);

                servicesFilters.remove(Service.DRAINAGE_SERVICE);
            }
        });
        drawerBinding.drawerDumpCheckBox.setOnClickListener(view -> {
            if (((CheckBox)view).isChecked()){
                servicesFilters.put(Service.DUMPSTER_SERVICE,new DumpService());
            }
            else{
                servicesFilters.remove(Service.DUMPSTER_SERVICE);
            }
        });
    }
    // affiche les lieux passés en paramètre sur la carte
    public void showMarkers(MapLocation... mapLocations){
        Log.i(TAG, "showMarkers called : "+ mapLocations.length+" markers");
        Log.i(TAG, "showMarkers: visible location has "+ locations.size()+ " locations");
        if (mMap != null) {
            Log.i(TAG, "showMarkers: mMap != null");
            mMap.clear();

            for (MapLocation l : mapLocations) {
                mMap.addMarker(new MarkerOptions().position(l.getPoint()).title(l.getId()));

            }
        }
        else{
            Log.i(TAG, "showMarkers: map ain't ready");
        }
    }

    //TODO si temps : prendre en compte booléen confirmed

    // filtre et affiche les lieux
    public void showFilteredLocations(ArrayList<MapLocation> mapLocations){
        if (mMap != null) {
            LatLngBounds searchArea = mMap.getProjection().getVisibleRegion().latLngBounds;
            MapLocationFilter.filterByMapBounds(searchArea, mapLocations);
            ArrayList<MapLocation> filteredLocations = null;
            if (servicesFilters != null && !servicesFilters.isEmpty()) {
                Log.i(TAG, "getLocationsOnScreen: filters are applied");
                filteredLocations = MapLocationFilter.filterByServices(servicesFilters, mapLocations);
                if (filteredLocations == null || filteredLocations.isEmpty()) {
                    Toast.makeText(getContext(), getString(R.string.location_filter_no_result), Toast.LENGTH_SHORT).show();
                } else {
                    showMarkers(filteredLocations.toArray(new MapLocation[filteredLocations.size()]));

                }
            } else {
                showMarkers(mapLocations.toArray(new MapLocation[mapLocations.size()]));
                Log.i(TAG, "getLocationsOnScreen: no filters applied");
            }

        }
    }





    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "onMapReady called");
        mMap = googleMap;

        //selectionne le thème de la carte
        if (theme.equals(getString(R.string.theme_dark))){
            Log.i(TAG,"theme preference is :" + theme);
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),R.raw.eg_map_night));
        }
        else if (theme.equals(getString(R.string.theme_light))){
            Log.i(TAG,"theme preference is :" + theme);

        }
        else{ Log.i(TAG,"theme preference unknown:" + theme);}



        // déplace la caméra sur la position de l'utilisateur si il est localisé'
        if (userLocation != null){
            //TODO si temps changer niveau de zoom dans settings
            Log.i(TAG, "onMapReady: moving camera to user location");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()),8));
        }
        else{ Log.i(TAG, "onMapReady: user location is null"); }

        // écoute des boutons en mode ajout de lieu
        if (adding){
            //clic simple : affiche un toast
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(@NonNull LatLng latLng) {
                    Toast.makeText(getContext(),getString(R.string.map_button_toast),Toast.LENGTH_LONG).show();
                }
            });
            // long clic : place un marqueur
            mMap.setOnMapLongClickListener(latLng -> {
                Log.i(TAG, "lat : " + latLng.latitude + "\nlong : "+ latLng.longitude);
                mMap.clear();

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(false)
                );


                // active la validation du lieu choisi
                binding.mapActionButton.setOnClickListener(view -> {
                    Intent pointIntent = new Intent("point_data");
                    pointIntent.putExtra(MAP_POINT_EXTRA_NAME,latLng);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pointIntent);
                    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                    dfs.setDecimalSeparator('.');
                    double trimmedLat = Double.parseDouble(new DecimalFormat("##.0000000",dfs).format(latLng.latitude));
                    double trimmedLng = Double.parseDouble(new DecimalFormat("##.0000000",dfs).format(latLng.longitude));

                    trimmedLatLng = new LatLng(trimmedLat,trimmedLng);
                    Log.i(TAG, "onMapReady: trimmed latLng : "+trimmedLatLng.latitude+ " | "+trimmedLatLng.longitude);
                    DatabaseService.startService(getContext(),MapsFragment.this);
                    showLoadScreen();
                });


            });
        }
        //écoute les boutons en mode exploration
        else {
            if (locations != null && locations.size() > 0){
                Log.i(TAG, "onMapReady: visible Locations are available");
                showFilteredLocations(locations);
            }


            // mise en place de la prévisualisation des lieux (InfoWindow)
            InfoWindow infoWindow = new InfoWindow(this);

            mMap.setInfoWindowAdapter(infoWindow);
//            mMap.setOnInfoWindowCloseListener(marker -> {
//            });
            mMap.setOnInfoWindowClickListener(infoWindow);
            mMap.setOnMapLongClickListener(latLng -> {
                Log.i(TAG, "lat : " + latLng.latitude + "\nlong : "+ latLng.longitude);

            });


            // bouton recherche
            binding.mapActionButton.setOnClickListener(view -> {
                Log.i(TAG, "onMapReady: map search button clicked");
                
                if (locations == null || locations.isEmpty()) {
                    Log.i(TAG, "onMapReady: location list is empty or null ");
                    DatabaseService.startService(getContext(), this);
                    showLoadScreen();
                }
                else if (locations.size() < initialLocationsCount){
                    Log.i(TAG, "onMapReady: locations list size was modified");
                    DatabaseService.startService(getContext(), this);
                    showLoadScreen();
                }
                else{
                    Log.i(TAG, "onMapReady: location list is available");
                    showFilteredLocations(locations);
                }

            });
        }




    }

    private void showLoadScreen(){
        binding.getRoot().setEnabled(false);
        binding.mapProgressBarContainer.setVisibility(View.VISIBLE);
    }
    private void hideLoadScreen(){
        binding.getRoot().setEnabled(true);
        binding.mapProgressBarContainer.setVisibility(View.GONE);
    }

    // callback passé au service récupérant les lieux enregistrés
    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        Log.i(TAG, "onComplete: Maps activity callback received");
        hideLoadScreen();

        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: task successfull");
            locations = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()){

                Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                locations.add(MapLocation.Builder.build(doc.getData()));
            }
            initialLocationsCount = locations.size();

            // mode ajout :  vérifie que le lieu n'est pas un doublon
            if (adding) {
                LatLngBounds bounds = new LatLngBounds(new LatLng(
                        (trimmedLatLng.latitude - 0.003),
                        (trimmedLatLng.longitude - 0.003)),
                        new LatLng((trimmedLatLng.latitude + 0.003),
                                (trimmedLatLng.longitude + 0.003))
                );
                ArrayList<MapLocation> closeLocations = MapLocationFilter.filterByMapBounds(bounds, locations);

                if (closeLocations.isEmpty()) { // pas de lieu a proximité
                    Log.i(TAG, "onComplete: no conflict for this location");
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(MAP_POINT_EXTRA_NAME, trimmedLatLng);
                    NavHostFragment.findNavController(MapsFragment.this).navigate(R.id.action_mapsFragment_to_AddLocationFragment, bundle);

                }
                else{ // lieu trouvé a proximité, possibilité de doublon
                    Log.i(TAG, "onComplete: location found in close proximity");
                    new AlertDialog.Builder(getContext())
                            .setTitle(getString(R.string.location_proximity_title))
                            .setMessage(getString(R.string.location_proximity_message))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable(MAP_POINT_EXTRA_NAME, trimmedLatLng);
                                    NavHostFragment.findNavController(MapsFragment.this).navigate(R.id.action_mapsFragment_to_AddLocationFragment, bundle);

                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, (dialogInterface, i) -> NavHostFragment.findNavController(MapsFragment.this).popBackStack())
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();




                }

            }
            else {
                showFilteredLocations(locations);
            }
        }
        else{
            Log.i(TAG, "onComplete: error while getting document");
        }
    }
    // getter pour l'arrayList contenant les lieux
    public ArrayList<MapLocation> getLocations() {
        if (locations == null){
            Log.i(TAG, "getVisibleLocations: array is null");
        }
        else{
            Log.i(TAG, "getVisibleLocations: array is not null");
        }
        return locations;
    }



    /* MapLocation methods */




    // callback sur le clic sur la prévisualisation d'un lieu : ouvre le LocationFragment
    @Override
    public void infoWindowClicked(MapLocation location) {
        Log.i(TAG, "infoWindowClicked called");

        Bundle bundle = new Bundle();
        bundle.putParcelable(MAPLOCATION_EXTRA_NAME,location);
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_mapsFragment_to_LocationFragment,bundle);


    }

    // callBack sur la localisation de l'utilisateur
    @Override
    public void onCompleteLocation(@NonNull Task<Location> task) {
        if (task.isSuccessful()){
            Log.i(TAG, "onLocationChecked: location successfull");
            userLocation = task.getResult();
            if (userLocation != null ){
                if (mMap != null) {
                    Log.i(TAG, "onLocationChecked: moving camera");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()),10));
                }
                else Log.i(TAG, "onLocationChecked: mMap is null");
            }
            else{
                Log.i(TAG, "onLocationChecked: userLocation is null");
            }
        }
        else{
            if (task.getException() != null){
                Log.i(TAG, "onLocationChecked: location failure : "+task.getException().getMessage());
            }
            else{
                Log.i(TAG, "onLocationChecked: location failure");
            }
        }
    }


    // getter/setter sur le Marker le plus récent (utilisé par la classe InfoWindow)
    public Marker getLastInfoWindowMarker() {
        return lastInfoWindowMarker;
    }

    public void setLastInfoWindowMarker(Marker lastInfoWindowMarker) {
        this.lastInfoWindowMarker = lastInfoWindowMarker;
    }
}