/*
 * Nom de classe : MenuFragment
 *
 * Description   : fragment affichant le menu principal.
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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import fr.abitbol.service4night.MainActivity;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.FragmentMenuBinding;
import fr.abitbol.service4night.listeners.OnSettingsNavigation;


public class MenuFragment extends Fragment implements OnSettingsNavigation {
    private final String TAG = "MenuFragment logging";
    private FragmentMenuBinding binding;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentMenuBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }


    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ajouter un lieu depuis la carte
        binding.addLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//
                Bundle arg = new Bundle();
                arg.putInt(MapsFragment.MAP_MODE_BUNDLE_NAME,MapsFragment.MAP_TYPE_ADD);
//                Intent intent = new Intent(getContext(),MapsFragment.class);
//                intent.putExtras(arg);
//
//                mGetContent.launch(intent);
                NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_mapsFragment,arg);


            }
        });

        // ajout de la position de l'utilisateur
        binding.directAddButton.setOnClickListener(button ->{

            if (MainActivity.fineLocation ){

                NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_AddLocationFragment);
            }
            else{
                Log.i(TAG, "onViewCreated: no location permissions, direct add unavailable");
                Toast.makeText(getContext(), getString(R.string.need_location_permissions), Toast.LENGTH_SHORT).show();
                getActivity().recreate();
            }
        });

        // explorer la carte
        binding.exploreButton.setOnClickListener(button -> {
            Bundle arg = new Bundle();
            arg.putInt(MapsFragment.MAP_MODE_BUNDLE_NAME,MapsFragment.MAP_TYPE_EXPLORE);
            NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_mapsFragment,arg);

        });
    }

    // ouverture du menu connexion
    public void navigateToSignIn(){
        NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_signInFragment);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ouverture du menu options de l'application
    @Override
    public void navigateToSettingsActivity() {
        NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_settingsActivity);

    }
}