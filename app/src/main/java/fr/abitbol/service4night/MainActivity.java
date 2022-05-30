/*
 * Nom de classe : MainActivity
 *
 * Description   : activité principale, gère la connection/inscription utilisateur, le NavHost et l'actionBar
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
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
import fr.abitbol.service4night.fragments.LocationAddFragment;
import fr.abitbol.service4night.fragments.MapsFragment;
import fr.abitbol.service4night.fragments.MenuFragment;
import fr.abitbol.service4night.listeners.OnSettingsNavigation;
import fr.abitbol.service4night.utils.NetworkReceiver;

public class MainActivity extends AppCompatActivity implements ActivityResultCallback<FirebaseAuthUIAuthenticationResult>, NavController.OnDestinationChangedListener{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseUser user;
    private FirebaseAuth auth;
    private boolean showSettings;
    public static final int LOCATION_REQUEST_CODE = 8631584;

    public static final String PREFERENCE_THEME_KEY= "theme";
    public static final String PREFERENCE_THEME_LIGHT= "Light";
    public static final String PREFERENCE_THEME_DARK= "Dark";
    public static final String PREFERENCE_THEME_DEFAULT= "Default";
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),this);
    private final String TAG = "MainActivity logging";
    private static final int menuFragmentId = 2131361800;
    private static final int mapsFragmentId = 2131362210;
    private NetworkReceiver networkReceiver;
    private AlertDialog networkAlertDialog;
    public static boolean prefChanged;
    boolean askingLocationPermissions;
    public static boolean coarseLocation = false;
    public static boolean fineLocation = false;
    public static boolean networkState = false;

    //TODO si temps: ajouter une note aux locations et un favori pour l'utilisateur
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            showSettings = true;
        }
        Log.i(TAG, "onCreate called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        askingLocationPermissions = false;
        auth = FirebaseAuth.getInstance();
        //TODO : tester si internet et sinon passer en mode hors ligne
        user = auth.getCurrentUser();

        // applique le thème choisi
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String theme = preferences.getString(PREFERENCE_THEME_KEY,PREFERENCE_THEME_DEFAULT);

        if(theme.equals(getString(R.string.theme_light))){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (theme.equals(getString(R.string.theme_dark))){
            Log.i(TAG,"theme preference is :" + theme);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else{ Log.i(TAG,"theme preference unknown:" + theme);}


        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        navController.addOnDestinationChangedListener(this);


        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);



    }

    public AlertDialog getNetworkAlertDialog() {
        return networkAlertDialog;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called ");
        // vérifie les permissions et les demande si nécessaire
        checkPermissions();
        if (!fineLocation || !networkState) {
            requestNeededPermissions();
        }
    }

    // vérifie les permissions accordées
    private void checkPermissions(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "checkPermissions: fine location is granted");
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i(TAG, "checkPermissions: gps provider is enabled");
                fineLocation = true;
            } else {
                Log.i(TAG, "checkPermissions: gps provider is disabled");
            }
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "checkPermissions: coarse location is granted");
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.i(TAG, "checkPermissions: network provider is enabled");
                coarseLocation = true;
            } else {
                Log.i(TAG, "checkPermissions: network provider is disabled");
            }

        }
        if (checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "checkPermissions: network state is granted");
            networkState = true;
        }
        else{
            Log.i(TAG, "checkPermissions: network state isn't granted");
        }
    }

    //demande les permissions localisation et surveillance du réseau
    private void requestNeededPermissions(){
        Log.i(TAG, "checkPermissions called");


        String[] permissions;
        int requestCode = LOCATION_REQUEST_CODE;

//        listener.onLocationChecked(fineLocation,coarseLocation);
        if(!fineLocation){

                Log.i(TAG, "onLocationChecked: asking permissions");

                if (!coarseLocation) {
                    if (networkState) {
                        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

                    } else {
                        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE};

                    }
                }
                else{
                    if (networkState) {
                        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
                    } else {
                        permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_NETWORK_STATE};
                    }

                }
                requestPermissions(permissions,requestCode);

        }
        else{
            Log.i(TAG, "checkLocationAccess: fine location available");
            if (networkState){
                Log.i(TAG, "checkPermissions: network state available");
            }
            else{
                Log.i(TAG, "checkPermissions: network state unavailable, requesting permission...");
                permissions = new String[]{Manifest.permission.ACCESS_NETWORK_STATE};
                requestPermissions(permissions,requestCode);
            }
        }



    }

    //gère le résultat des demandes de permissions et ferme l'application si les permissions obligatoires sont refusées
    private void processPermissionsRequestResult(){
        checkPermissions();
        if (networkState) {
            if (!fineLocation) {
                if (!coarseLocation) {
                    Log.i(TAG, "onLocationChecked: permissions asked : couldn't access location");
                    Toast.makeText(this, getString(R.string.no_localisation_permissions), Toast.LENGTH_SHORT).show();
                    delayedFinish();

                }
                else{
                    Log.i(TAG, "checkLocationAccess: coarse location only");
                    Toast.makeText(this, getString(R.string.coarse_localisation_limit), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.i(TAG, "processPermissionsRequestResult: all permissions granted");
            }
        } else {
            if (!fineLocation) {
                Toast.makeText(this, getString(R.string.no_localisation_and_network_permissions), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.no_network_permission), Toast.LENGTH_SHORT).show();
            }
            delayedFinish();
        }
    }
    // retard la fermeture de l'application pour laisser a l'utilisateur le temps de lire le toast
    private void delayedFinish(){
        new Thread(() -> {
            synchronized (this){
                try {
                    wait(5000);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG, "onRequestPermissionsResult called");

        if (requestCode == LOCATION_REQUEST_CODE) {
            processPermissionsRequestResult();
        }
    }

    // interception de l'appui sur la touche retour
    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: back button pressed ");
        Fragment fragment = getVisibleFragment();
        if (fragment != null ){

            // si une infoWindow est ouverte sur la carte, la touche retour la referme
            Log.i(TAG, "onBackPressed: visible fragment is : " +fragment.toString()+" id : "+fragment.getId());
            if (fragment instanceof MapsFragment || fragment.getId() == mapsFragmentId) {
                Log.i(TAG, "onBackPressed: fragment is instance of MapsFragment");
                if (((MapsFragment)fragment).getLastInfoWindowMarker() != null && ((MapsFragment)fragment).getLastInfoWindowMarker().isInfoWindowShown()){
                    ((MapsFragment)fragment).getLastInfoWindowMarker().hideInfoWindow();
                }
                else{
                    super.onBackPressed();
                }
            }
            else if (fragment instanceof LocationAddFragment){
                System.out.println("onBackPressed : fragment is LocationAddFragment");
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.pop_back_abort_title))
                        .setMessage(getString(R.string.pop_back_abort))
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
//                                NavHostFragment.findNavController(fragment).popBackStack();
                                MainActivity.super.onBackPressed();

                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }


            else {
                super.onBackPressed();
            }
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu called");
        // affiche le menu correspondant au statut utilisateur et au fragment affiché
        if (showSettings) {
            if (user != null){// menu connecté
                Log.i(TAG, "onCreateOptionsMenu: user is valid : " + user.getEmail());

                getMenuInflater().inflate(R.menu.menu_logged, menu);
            }
            else{ // menu non connecté
                Log.i(TAG, "onCreateOptionsMenu: user is null");
                getMenuInflater().inflate(R.menu.menu_main, menu);
            }
        } else { // menu avec options cachées (hors menu principal)
            Log.i(TAG, "onCreateOptionsMenu: hiding settings");
            getMenuInflater().inflate(R.menu.menu_empty, menu);
        }


        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Log.i(TAG, "onOptionsItemSelected called item : "+ item);
        // clic sur menu options du compte
        if(item.getItemId() == R.id.account_settings_item){
            if (user != null) {
                Fragment fragment = getVisibleFragment();
                if (fragment instanceof OnSettingsNavigation) {
                    Log.i(TAG, "onOptionsItemSelected: fragment does implements OnSettingsNavigation");
                    NavHostFragment.findNavController(fragment).navigate(R.id.action_MenuFragment_to_account_settings_nav_graph);
                }
            }
            else{
                Toast.makeText(this, getString(R.string.not_logged_in), Toast.LENGTH_SHORT).show();
            }
        }
        // interception du bouton home pour demander confirmation avant de quitter l'ajout de lieu
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
        // clic sur menu options de l'application
        else if (item.getItemId() == R.id.app_settings_item){
            //TODO ajouter option vider cache images et vider cache sauf favoris
//            Intent appSettingsIntent = new Intent(getApplicationContext(),ApplicationSettingsActivity.class);
//            startActivity(appSettingsIntent);
            Fragment fragment = getVisibleFragment();
            if (fragment instanceof OnSettingsNavigation) {
                Log.i(TAG, "onOptionsItemSelected: fragment does implements OnSettingsNavigation");
                NavHostFragment.findNavController(fragment).navigate(R.id.action_MenuFragment_to_settingsActivity);
            }

        }
        // clic sur connexion
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
        // clic sur déconnexion
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
        // clic sur inscription
        else if (item.getItemId() == R.id.sign_in_item){
            Fragment fragment = getVisibleFragment();
            if (fragment instanceof MenuFragment){
                Log.i(TAG, "onOptionsItemSelected: visible fragment is a MenuFragment");
                ((MenuFragment) fragment).navigateToSignIn();
            }

        }

        return true;
    }

    // renvoie le fragment affiché dans le NavHostFragment
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
        unregisterReceiver(networkReceiver);
        Log.i(TAG,"onPause called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG,"onResume called");
        // actualise si les options ont été modifiées
        if (prefChanged){
            Log.i(TAG,"prefChanged is true");
            prefChanged = false;
            recreate();
        }
        // met l'actionBar a jour
        invalidateOptionsMenu();

        // inscrit le NetworkReceiver
        networkReceiver = NetworkReceiver.getInstance();
        IntentFilter networkIntentFilter = new IntentFilter();
        networkIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver,networkIntentFilter);



    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }



    // callBack sur l'authentification
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
    // gère la visibilité des options selon l'id du fragment passé en paramètre
    public void manageMenu(int id){
        Fragment fragment = getVisibleFragment();

        if (fragment != null && (getVisibleFragment() instanceof MenuFragment || getVisibleFragment().getId() == menuFragmentId)) {
            Log.i(TAG, "manageMenu: visible fragment is the menu fragment");
            setSettingsVisibility(true);
            setActionBarVisibility(true);
        }
        else if (id == menuFragmentId){
            Log.i(TAG, "manageMenu: destination is menu fragment");
            setSettingsVisibility(true);
            setActionBarVisibility(true);
        }
        else{
            setSettingsVisibility(false);
            Log.i(TAG, "manageMenu: visible fragment is not the menu fragment");
        }




    }
    // affiche la boite de dialogue d'erreur de déconnexion
    public void showNetworkError(){
        Log.i(TAG, "showNetworkError called");
        networkAlertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.network_error_title))
                .setMessage(getString(R.string.network_error_message))
                .setPositiveButton(R.string.network_error_quit_option, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.

                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();


    }
    public void setActionBarVisibility(boolean b){
        Log.i(TAG, "setActionBarVisibility called : "+ b);
        if (getSupportActionBar()!=null) {
            if (b){
                getSupportActionBar().show();
            }
            else{
                getSupportActionBar().hide();
            }

        }
    }
    public void setTitle(String title) throws NullPointerException{
        binding.toolbar.setTitle(title);
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