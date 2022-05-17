package fr.abitbol.service4night;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.abitbol.service4night.DAO.DAOFactory;
import fr.abitbol.service4night.DAO.LocationDAO;
import fr.abitbol.service4night.databinding.ActivityMapsBinding;
import fr.abitbol.service4night.databinding.DrawerFilterBinding;
import fr.abitbol.service4night.listeners.OnCompleteLocalisationListener;
import fr.abitbol.service4night.listeners.OnInfoWindowClickedAdapter;
import fr.abitbol.service4night.services.DrainService;
import fr.abitbol.service4night.services.DumpService;
import fr.abitbol.service4night.services.ElectricityService;
import fr.abitbol.service4night.services.InternetService;
import fr.abitbol.service4night.services.Service;
import fr.abitbol.service4night.services.WaterService;
import fr.abitbol.service4night.utils.UserLocalisation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnCompleteListener<QuerySnapshot>, OnCompleteLocalisationListener, OnInfoWindowClickedAdapter {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private DrawerFilterBinding drawerBinding;
    private Location userLocation;
    private LocationDAO dataBase;
    private ArrayList<MapLocation> visibleLocations;
    private Map<String,Service> servicesFilters;
    private final String TAG = "mapsActivity logging";
    private String theme;
    public static final int MAP_TYPE_EXPLORE = 3737;
    public static final int MAP_TYPE_ADD = 7337;
    public static final String MAP_MODE_BUNDLE_NAME = "mapMode";
    public static final String MAP_POINT_EXTRA_NAME = "point";
    public static final String ACTION_GET_POINT_LATLNG = "point_data";
    public static final int LOCATION_REQUEST_CODE = 8631584;
    public static final String MAPLOCATION_EXTRA_NAME = "mapLocation";
    private static final int BACKGROUND_LOCATION_REQUEST_CODE = 8631554;
    boolean adding;
    boolean searchStarted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        //Navigation.findNavController(this,R.id.map).getGraph().

        if (savedInstanceState == null) {
            searchStarted = false;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            theme = preferences.getString(MainActivity.PREFERENCE_THEME_KEY,MainActivity.PREFERENCE_THEME_DEFAULT);
            if(theme.equals(MainActivity.PREFERENCE_THEME_LIGHT)){
                Log.i(TAG,"theme preference is :" + theme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            else if (theme.equals(MainActivity.PREFERENCE_THEME_DARK)){
                Log.i(TAG,"theme preference is :" + theme);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else{ Log.i(TAG,"theme preference unknown:" + theme);}

            //TODO : localiser l'utilisateur pour centrer la map
            Log.i(TAG, "onCreate: starting localisation");
            new UserLocalisation(this).locateUser(this);
            visibleLocations = null;
            binding = ActivityMapsBinding.inflate(getLayoutInflater());
            switch (getIntent().getIntExtra(MAP_MODE_BUNDLE_NAME,-1)){
                // TODO set popBack in navGraph on map or on main menu
                case MAP_TYPE_ADD:
                    adding = true;
                    binding.mapActionButton.setImageResource(R.drawable.ic_done);
                    break;
                case  MAP_TYPE_EXPLORE:
                    adding = false;
                    dataBase = DAOFactory.getLocationDAOReadOnly(MapsActivity.this);


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
                        if (!servicesFilters.isEmpty()){
                            Log.i(TAG, "onCreate: some filters are selected ");

                        }
                        else {
                            Log.i(TAG, "onCreate: serviceFilters is empty ");
                        }
                        if (visibleLocations != null) {
                            binding.mapActionButton.callOnClick();
                        }

                    });


    //                drawerBinding.getRoot().setOnTouchListener(new View.OnTouchListener() {
    //                    @Override
    //                    public boolean onTouch(View view, MotionEvent motionEvent) {
    //                        return true;
    //                    }
    //                });
                    break;
                default:
                    Log.i(TAG, "onCreate: no extra in map intent");
                    finish();


            }



            setContentView(binding.getRoot());
            //Log.i(TAG, "onCreate: extra "+ getIntent().getStringExtra(MAP_MODE_BUNDLE_NAME));


            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);


            binding.mapActionButton.show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume called");
        super.onResume();


    }
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

    public void showMarkers(MapLocation... mapLocations){
        if (mMap != null) {
            mMap.clear();

            for (MapLocation l : mapLocations) {
                mMap.addMarker(new MarkerOptions().position(l.getPoint()).title(l.getId()));

            }
        }
    }

    //TODO: prendre en compte booléen confirmed
    //TODO : ajouter un bouton annuler filtres au drawer
    public void showFilteredLocations(List<MapLocation> mapLocations){
        //TODO si temps : faire une tri dans les locations au niveau de la requëte base de données
        if (mMap != null){
            LatLngBounds searchArea = mMap.getProjection().getVisibleRegion().latLngBounds;
            MapLocationFilter.filterByLocation(searchArea, mapLocations);
            if (servicesFilters != null && !servicesFilters.isEmpty()){
                Log.i(TAG, "getLocationsOnScreen: filters are applied");
                MapLocationFilter.filterByServices(servicesFilters, mapLocations);
            }
            else{ Log.i(TAG, "getLocationsOnScreen: no filters applied");}
            showMarkers(mapLocations.toArray(new MapLocation[mapLocations.size()]));
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
        if (theme.equals("Dark")){
            Log.i(TAG,"theme preference is :" + theme);
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.eg_map_night));
        }
        else if (theme.equals("Light")){
            Log.i(TAG,"theme preference is :" + theme);

        }
        else{ Log.i(TAG,"theme preference unknown:" + theme);}
        if (userLocation != null){
            //TODO changer niveau de zoom dans settings
            Log.i(TAG, "onMapReady: moving camera to user location");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLocation.getLatitude(),userLocation.getLongitude()),8));
        }
        else{ Log.i(TAG, "onMapReady: user location is null"); }

        if (adding){
            mMap.setOnMapLongClickListener(latLng -> {
                Log.i(TAG, "lat : " + latLng.latitude + "\nlong : "+ latLng.longitude);
                mMap.clear();

                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(false)
                );


                binding.mapActionButton.setOnClickListener(view -> {
                    //TODO vérifier si la location n'existe pas déja
                    Intent pointIntent = new Intent("point_data");
                    pointIntent.putExtra(MAP_POINT_EXTRA_NAME,latLng);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pointIntent);
                    setResult(MAP_TYPE_ADD,pointIntent);

                    finish();
                });


            });
        }
        else {

            //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

            //TODO : fermer infowindow si clic sur retour
            InfoWindow infoWindow = new InfoWindow(MapsActivity.this);
            mMap.setInfoWindowAdapter(infoWindow);
            mMap.setOnInfoWindowClickListener(infoWindow);
            mMap.setOnMapLongClickListener(latLng -> {
                Log.i(TAG, "lat : " + latLng.latitude + "\nlong : "+ latLng.longitude);

            });

//        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(@NonNull LatLng latLng) {
//                Toast.makeText(getApplicationContext(),"long click to select location",Toast.LENGTH_LONG).show();
//            }
//        });

            binding.mapActionButton.setOnClickListener(view -> {
                Log.i(TAG, "onMapReady: map search button clicked");
                DatabaseBindService.startService(MapsActivity.this,this);

                //TODO : show dialog during locations fetch
            });
        }




    }


    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        Log.i(TAG, "onComplete: Maps activity callback received");
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: task successfull");
            visibleLocations  = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()){

                Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                visibleLocations.add(MapLocation.Builder.build(doc.getData()));
            }
            showFilteredLocations(visibleLocations);

        }
        else{
            Log.i(TAG, "onComplete: error while getting document");
        }
    }

    public ArrayList<MapLocation> getVisibleLocations() {
        if (visibleLocations == null){
            Log.i(TAG, "getVisibleLocations: array is null");
        }
        else{
            Log.i(TAG, "getVisibleLocations: array is not null");
        }
        return visibleLocations;
    }



    /* MapLocation methods */





    @Override
    public void infoWindowClicked(MapLocation location) {
        Log.i(TAG, "infoWindowClicked called");
        Intent pointIntent = new Intent(ACTION_GET_POINT_LATLNG);

        pointIntent.putExtra(MAPLOCATION_EXTRA_NAME, (Parcelable) location);
        setResult(MAP_TYPE_EXPLORE,pointIntent);

        finish();
    }

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





    /* bound service response handler */


}