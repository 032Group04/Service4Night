package fr.abitbol.service4night;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;

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

public class MainActivity extends AppCompatActivity implements ActivityResultCallback<FirebaseAuthUIAuthenticationResult>, NavController.OnDestinationChangedListener {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseUser user;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new FirebaseAuthUIActivityResultContract(),this);
    private  final String TAG = "MainActivity logging";

    public static boolean prefChanged;

    //TODO: ajouter une note aux locations et un favori pour l'utilisateur
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());

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



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu called");

        if (user != null){
            Log.i(TAG, "onCreateOptionsMenu: user is valid : " + user.getEmail());

            getMenuInflater().inflate(R.menu.menu_logged, menu);
        }
        else{
            Log.i(TAG, "onCreateOptionsMenu: user is null");
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }


        return true;
    }

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
                Log.i(TAG, "getvisiblefragment returns a fragment");
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

    @Override
    public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
        Log.i(TAG, "onDestinationChanged called");
    }
}