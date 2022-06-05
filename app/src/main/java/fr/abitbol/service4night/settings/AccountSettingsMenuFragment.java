/*
 * Nom de classe : AccountsettingsMenuFragment
 *
 * Description   : fragment des options du compte
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.settings;

import android.app.ActionBar;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.FragmentAccountSettingsMenuBinding;


public class AccountSettingsMenuFragment extends Fragment {


    private static final String TAG = "AccountSettingsMenuFragment logging";
    private FragmentAccountSettingsMenuBinding binding;



    public AccountSettingsMenuFragment() {

        // Required empty public constructor
    }
//    private void setTitle() {
//
//
//        /*
//         * modification du titre
//         */
//
//        if (getActivity() != null) {
//            Log.i(TAG, "onViewCreated: main activity not null");
//            ActionBar actionBar = getActivity().getActionBar();
//            if (actionBar != null) {
//                Log.i(TAG, "onViewCreated: action bar not null");
//                actionBar.setTitle(getString(R.string.title_user_locations_list));
//
//            } else {
//                Log.i(TAG, "onViewCreated: action bar is null");
//            }
//
//
//
//        } else Log.i(TAG, "onViewCreated: mainActivity is null");
//
//
//
//    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        binding = FragmentAccountSettingsMenuBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.modifyLocationsButton.setOnClickListener(v -> {
            NavHostFragment.findNavController(AccountSettingsMenuFragment.this).navigate(R.id.action_accountSettingsMenuFragment_to_userLocationsFragment);
        });
    }
}