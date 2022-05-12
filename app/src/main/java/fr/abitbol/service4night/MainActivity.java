package fr.abitbol.service4night;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;


import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;


import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import fr.abitbol.service4night.databinding.ActivityMainBinding;
import fr.abitbol.service4night.listeners.OnSettingsNavigation;

public class MainActivity extends AppCompatActivity implements ActivityResultCallback<FirebaseAuthUIAuthenticationResult>, NavController.OnDestinationChangedListener{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private boolean showSettings;
    public static final int LOCATION_REQUEST_CODE = 8631584;
    public static final int FINE_LOCATION_REQUEST_CODE = 8631547;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),this);
    private final String TAG = "MainActivity logging";
    private static final int menuFragmentId = 2131361800;
    public static boolean prefChanged;
    boolean askingPermissions;
    public static boolean coarseLocation = false;
    public static boolean fineLocation = false;

    //TODO: ajouter une note aux locations et un favori pour l'utilisateur
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            showSettings = true;
        }
        Log.i(TAG, "onCreate called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        askingPermissions = false;
        auth = FirebaseAuth.getInstance();
        //TODO : gérer comptes anonymes
        //TODO : tester si internet et sinon passer en mode hors ligne
        user = auth.getCurrentUser();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = preferences.getString("theme","unknown");

        if(theme.equals("Light")){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (theme.equals("Dark")){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{ Log.i(TAG,"theme preference unknown:" + theme);}


        //TODO : cacher toolBar dans la map
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.addOnDestinationChangedListener(this);


        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



    }
    public void manageToolBar(){

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called ");


        checkLocationAccess();


    }
    private void checkLocationAccess(){
        Log.i(TAG, "startLocation called");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "startLocation: fine location is granted");
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "startLocation: gps provider is enabled");
                fineLocation = true;
            } else {
                Log.i(TAG, "startLocation: gps provider is disabled");
            }
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "startLocation: coarse location is granted");
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.i(TAG, "startLocation: network provider is enabled");
                coarseLocation = true;
            } else {
                Log.i(TAG, "startLocation: network provider is disabled");
            }

        }
//        listener.onLocationChecked(fineLocation,coarseLocation);
        if(!fineLocation){
            if (!askingPermissions) {
                Log.i(TAG, "onLocationChecked: asking permissions");
                askingPermissions = true;
                if (!coarseLocation) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);

                }
            } else {
                if (!coarseLocation) {
                    Log.i(TAG, "onLocationChecked: permissions asked : couldn't access location");
                    Toast.makeText(this, getString(R.string.no_localisation_permissions), Toast.LENGTH_SHORT).show();

                }
                else{
                    Log.i(TAG, "checkLocationAccess: coarse location only");
                    Toast.makeText(this, getString(R.string.coarse_localisation_limit), Toast.LENGTH_SHORT).show();
                }
            }
        }
        else{
            Log.i(TAG, "checkLocationAccess: fine location available");
        }



    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult called");
        if (requestCode == LOCATION_REQUEST_CODE){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Log.i(TAG, "onRequestPermissionsResult: at least one location permissions was granted");
                checkLocationAccess();

            }
            else{
                Log.i(TAG, "onRequestPermissionsResult: location permissions were declined");
            }
        }
        else if (requestCode == FINE_LOCATION_REQUEST_CODE){
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                Log.i(TAG, "onRequestPermissionsResult: fine location permission was granted");
                checkLocationAccess();

            }
            else{
                Log.i(TAG, "onRequestPermissionsResult: location permissions were declined");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu called");

        if (showSettings) {
            if (user != null){
                Log.i(TAG, "onCreateOptionsMenu: user is valid : " + user.getEmail());

                getMenuInflater().inflate(R.menu.menu_logged, menu);
            }
            else{
                Log.i(TAG, "onCreateOptionsMenu: user is null");
                getMenuInflater().inflate(R.menu.menu_main, menu);
            }
        } else {
            Log.i(TAG, "onCreateOptionsMenu: hiding settings");
            getMenuInflater().inflate(R.menu.menu_empty, menu);
        }


        return true;
    }
    //TODO : gérer visibilité des menus selon connecté ou non
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected called item : "+ item);
        // TODO : ajouter modification du compte

        if(item.getItemId() == R.id.account_settings_item){
            
        }
        else if (item.getItemId() == android.R.id.home){
            Log.i(TAG, "onOptionsItemSelected: home button clicked");
            Fragment fragment = getVisibleFragment();
            if (fragment != null){
                if (fragment instanceof LocationAddFragment){
                    //TODO : si temps : sauvegarder données location dans sharedPreferences pour reprendre plus tard
                    new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.pop_back_abort_title))
                        .setMessage(getString(R.string.pop_back_abort))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                NavHostFragment.findNavController(fragment).popBackStack();

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                }
                else {
                    NavHostFragment.findNavController(fragment).popBackStack();
                }
            }
        }

        else if (item.getItemId() == R.id.app_settings_item){
            //TODO ajouter option vider cache images et vider cache sauf favoris
//            Intent appSettingsIntent = new Intent(getApplicationContext(),SettingsActivity.class);
//            startActivity(appSettingsIntent);
            Fragment fragment = getVisibleFragment();
            if (fragment instanceof OnSettingsNavigation) {
                Log.i(TAG, "onOptionsItemSelected: fragment does implements OnSettingsNavigation");
                NavHostFragment.findNavController(fragment).navigate(R.id.action_MenuFragment_to_settingsActivity);
            }

        }
        else if (item.getItemId() == R.id.log_in_item){
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.EmailBuilder().build(),
                    new AuthUI.IdpConfig.GoogleBuilder().build()
            );

            launcher.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
            );
        }
        else if (item.getItemId() == R.id.log_out_item){
            AuthUI.getInstance().signOut(this).addOnCompleteListener(task -> {
               if (task.isSuccessful()){
                   user = null;
                   Log.i(TAG, "onOptionsItemSelected: logged out successfully");
                   Toast.makeText(this, getString(R.string.log_out_success), Toast.LENGTH_SHORT).show();
                   recreate();
               }
               else{
                   Log.i(TAG, "onOptionsItemSelected: logged out fail");
                   Toast.makeText(this, getString(R.string.log_out_failed), Toast.LENGTH_SHORT).show();

               }
            });
        }
        else if (item.getItemId() == R.id.sign_in_item){
            Fragment fragment = getVisibleFragment();
            if (fragment instanceof MenuFragment){
                Log.i(TAG, "onOptionsItemSelected: visible fragment is a MenuFragment");
                ((MenuFragment) fragment).navigateToSignIn();
            }

        }

        return true;
    }
    private Fragment getVisibleFragment(){

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("fragment_container");
        List<Fragment> fragments = fragment.getChildFragmentManager().getFragments();
        for (Fragment f : fragments){
            if(f!=null && f.isVisible()){
                Log.i(TAG, "getvisiblefragment returns a fragment : tostring = " + f.toString()+ " "+f.getId());
                return f;
            }
        }
        Log.i(TAG, "getvisiblefragment returns null");
        return null;


    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG,"onPause called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume called");
        if (prefChanged){
            Log.i(TAG,"prefChanged is true");
            prefChanged = false;
            recreate();
        }



    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }


    public void lockScreen(){
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }
    public void unlockScreen(){
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    @Override
    public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
        IdpResponse response = result.getIdpResponse();
        if (result.getResultCode() == RESULT_OK){
            Log.i(TAG, "onActivityResult: loggin success");
            Toast.makeText(this, getString(R.string.log_in_success), Toast.LENGTH_SHORT).show();
            user = FirebaseAuth.getInstance().getCurrentUser();
            //recreate();
        }
        else{
            if ((response == null)){
                Log.i(TAG, "onActivityResult: user canceled login");
            }
            else{
                if (response.getError() != null) {
                    Log.e(TAG, "onActivityResult: connection failed :" + response.getError().getMessage());
                    Toast.makeText(this, getString(R.string.log_in_failed) + response.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.e(TAG, "onActivityResult: connection failed");
                    Toast.makeText(this, getString(R.string.log_in_failed) , Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void manageMenu(){
        if (getVisibleFragment() instanceof MenuFragment){
            Log.i(TAG, "manageMenu: visible fragment is the menu fragment");
            setSettingsVisibility(true);
        }
        else{
            Log.i(TAG, "manageMenu: visible fragment is not the menu fragment");
        }
    }
    //TODO: essayer copmprendre fragment visible
    public void manageMenu(int id){
        Fragment fragment = getVisibleFragment();

        if (fragment != null && (getVisibleFragment() instanceof MenuFragment || getVisibleFragment().getId() == menuFragmentId)) {
            Log.i(TAG, "manageMenu: visible fragment is the menu fragment");
            setSettingsVisibility(true);
        }
        else if (id == menuFragmentId){
            Log.i(TAG, "manageMenu: destination is menu fragment");
            setSettingsVisibility(true);
        }
        else{
            Log.i(TAG, "manageMenu: visible fragment is not the menu fragment");
        }




    }
    public void setSettingsVisibility(boolean b){
        Log.i(TAG, "setSettingsVisibility called : "+b);
        showSettings = b;
        invalidateOptionsMenu();

    }

    @SuppressLint("ResourceType")
    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        Log.i(TAG, "onDestinationChanged called :id= "+ destination.getId()+ "name ="+destination.getNavigatorName()+"label = "+destination.getLabel());
        manageMenu((destination.getLabel()!=null)? destination.getId(): -1);

    }
}