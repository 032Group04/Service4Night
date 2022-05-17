package fr.abitbol.service4night;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import fr.abitbol.service4night.databinding.ActivityApplicationSettingsBinding;


public class ApplicationSettingsActivity extends AppCompatActivity {

    private final String TAG = "ApplicationSettingsActivity logging";
    public static final String THEME_PREFERENCE_KEY = "theme";
    public static final String THEME_DARK ="Dark";
    public static final String THEME_LIGHT ="Light";
    private ActivityApplicationSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        binding =ActivityApplicationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (savedInstanceState == null){
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.settingsContainer,new SettingsFragment())
                    .commit();
        }

    }

    public static class SettingsFragment extends PreferenceFragmentCompat{
        private final String TAG = "ApplicationSettingsFragment logging";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            Log.i(TAG, "onCreatePreferences called");
            addPreferencesFromResource(R.xml.preferences_application);
        }

        @Override
        public void onDisplayPreferenceDialog(@NonNull Preference preference) {
            super.onDisplayPreferenceDialog(preference);
            Log.i(TAG, "onDisplayPreferenceDialog called");
        }

        @Override
        public boolean onPreferenceTreeClick(Preference preference) {
            preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    Log.i(TAG,"preference listener triggered");
                    MainActivity.prefChanged = true;
                    if (preference.getKey().equals(THEME_PREFERENCE_KEY)){
                        Log.i(TAG, "onPreferenceChange: theme was changed");
                        if(((String)newValue).equals(THEME_LIGHT)){
                            Log.i(TAG,"theme preference is :" + THEME_LIGHT);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        }
                        else if(((String)newValue).equals(THEME_DARK)){

                            Log.i(TAG,"theme preference is :" + THEME_DARK);
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        }
                    }

//                    try {
//                        getActivity().recreate();
//                    }catch (NullPointerException e){
//                        Log.e(TAG, "onPreferenceChange: couldn't recreate: NullpointerException");
//                    }
                    return true;
                }

            });

            Log.i(TAG," after listener preference is: " + preference.getSummary());
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i("ApplicationSettingsActivity call","onDestroy called");
    }
}