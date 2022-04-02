package fr.abitbol.service4night;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import fr.abitbol.service4night.databinding.ActivityMapsBinding;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnCompleteListener<QuerySnapshot> {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private DataBase dataBase;

    private final String TAG = "mapsActivity logging";
    public static final int MAP_TYPE_EXPLORE = 3737;
    public static final int MAP_TYPE_ADD = 7337;
    public static final String MAP_MODE_BUNDLE_NAME = "mapMode";
    public static final String MAP_POINT_BUNDLE_NAME = "point";
    boolean adding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Navigation.findNavController(this,R.id.map).getGraph().

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        switch (getIntent().getIntExtra(MAP_MODE_BUNDLE_NAME,-1)){
            case MAP_TYPE_ADD:
                adding = true;
                binding.mapActionButton.setImageResource(R.drawable.ic_done);
                break;
            case  MAP_TYPE_EXPLORE:
                adding = false;
                dataBase = new DataBase();
                binding.mapActionButton.setImageResource(R.drawable.ic_search);
                LayoutInflater inflater =(LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View drawer = (View) inflater.inflate(R.layout.drawer_filter, (ViewGroup) findViewById(R.id.drawer));
                binding.getRoot().addView(drawer);
                break;
            default:
                Log.i(TAG, "onCreate: no extra in map intent");
                finish();


        }



        setContentView(binding.getRoot());
        Log.i(TAG, "onCreate: extra "+ getIntent().getStringExtra(MAP_MODE_BUNDLE_NAME));



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



        binding.mapActionButton.show();
    }



    public void showMarkers(Location... locations){
        if (mMap != null) {
            mMap.clear();

            for (Location l : locations) {
                mMap.addMarker(new MarkerOptions().position(l.getPoint()).title(l.getId()));

            }
        }
    }
    public void filterLocations(ArrayList<Location> locations){
        if (mMap != null){
            LatLngBounds searchArea = mMap.getProjection().getVisibleRegion().latLngBounds;
            Log.i(TAG, "before filtering arraylist size is: " + locations.size());
            locations.removeIf(location -> !(searchArea.contains(location.getPoint())));
            Log.i(TAG, "after filtering arraylist size is: " + locations.size());
            showMarkers(locations.toArray(new Location[locations.size()]));
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
        mMap = googleMap;

        if (adding){
            mMap.setOnMapLongClickListener(latLng -> {
                Log.i(TAG, "lat : " + latLng.latitude + "\nlong : "+ latLng.longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(false)
                );


                binding.mapActionButton.setOnClickListener(view -> {
                    Intent pointIntent = new Intent("point_data");
                    pointIntent.putExtra(MAP_POINT_BUNDLE_NAME,latLng);
//            LocalBroadcastManager.getInstance(this).sendBroadcast(pointIntent);
                    setResult(MAP_TYPE_ADD,pointIntent);

                    finish();
                });


            });
        }
        else {

            //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            mMap.setInfoWindowAdapter(new InfoWindow(MapsActivity.this));

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
                dataBase.callLocations("locations",MapsActivity.this);


            });
        }




    }


    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        Log.i(TAG, "onComplete: Maps activityu callback received");
        if (task.isSuccessful()){
            ArrayList<Location> locations  = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()){
                Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                locations.add(LocationBuilder.build(doc.getData()));
            }
            filterLocations(locations);
        }
        else{
            Log.i(TAG, "onComplete: error' getting document");
        }
    }
}