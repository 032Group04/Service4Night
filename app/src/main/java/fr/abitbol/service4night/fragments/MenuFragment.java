package fr.abitbol.service4night.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

        //TODO : ajouter un message sur les modes de connexion
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
        binding.directAddButton.setOnClickListener(button ->{
            //TODO vérifier si la location n'existe pas déja

            if (MainActivity.fineLocation || MainActivity.coarseLocation){

                NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_AddLocationFragment);
            }
            else{
                Log.i(TAG, "onViewCreated: no location permissions, direct add unavailable");
                Toast.makeText(getContext(), getString(R.string.need_location_permissions), Toast.LENGTH_SHORT).show();
                getActivity().recreate();
            }
        });
        binding.exploreButton.setOnClickListener(button -> {
            Bundle arg = new Bundle();
            arg.putInt(MapsFragment.MAP_MODE_BUNDLE_NAME,MapsFragment.MAP_TYPE_EXPLORE);
            NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_mapsFragment,arg);

        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == 42){
//            Log.i(TAG, "onActivityResult: "+ data.getExtras().getParcelable("point").toString());
//            NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_AddLocationFragment,data.getExtras());
//        }
//    }
    public void navigateToSignIn(){
        NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_signInFragment);
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    @Override
    public void navigateToSettingsActivity() {
        NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_settingsActivity);

    }
}