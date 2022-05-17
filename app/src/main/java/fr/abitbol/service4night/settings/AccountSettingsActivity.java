package fr.abitbol.service4night.settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;

import android.os.Bundle;

import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.ActivityAccountSettingsBinding;

public class AccountSettingsActivity extends AppCompatActivity {
    private ActivityAccountSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

}