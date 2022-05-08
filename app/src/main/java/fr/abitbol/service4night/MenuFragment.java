package fr.abitbol.service4night;

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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;

import fr.abitbol.service4night.databinding.FragmentMenuBinding;


public class MenuFragment extends Fragment implements OnSettingsNavigation {
    private final String TAG = "MenuFragment logging";
    private FragmentMenuBinding binding;
    private final ActivityResultLauncher<Intent> mGetContent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    switch (result.getResultCode()) {
                        case MapsActivity.MAP_TYPE_ADD:
                            //Log.i(TAG, "onActivityResult: map adding point result received : " + result.getData().getBundleExtra(MapsActivity.MAP_POINT_BUNDLE_NAME));
                            NavHostFragment.findNavController(MenuFragment.this)
                                .navigate(R.id.action_MenuFragment_to_AddLocationFragment, result.getData().getExtras());
                            break;
                        case MapsActivity.MAP_TYPE_EXPLORE:
                            NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_LocationFragment,result.getData().getExtras());
                            break;
                    }
                }
            });

//    private BroadcastReceiver PointReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i(TAG, "onReceive: activity : " + getActivity().getLocalClassName());
//
//
//        }
//    };

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
                arg.putInt(MapsActivity.MAP_MODE_BUNDLE_NAME,MapsActivity.MAP_TYPE_ADD);
                Intent intent = new Intent(getContext(),MapsActivity.class);
                intent.putExtras(arg);

                mGetContent.launch(intent);


            }
        });
        binding.directAddButton.setOnClickListener(button ->{
            NavHostFragment.findNavController(MenuFragment.this).navigate(R.id.action_MenuFragment_to_AddLocationFragment);
        });
        binding.exploreButton.setOnClickListener(button -> {
//            Bundle arg = new Bundle();
//            arg.putInt(MapsActivity.MAP_MODE_KEY,MapsActivity.MAP_TYPE_EXPLORE);
//            LocalBroadcastManager.getInstance(getContext()).registerReceiver(PointReceiver,new IntentFilter("point_event"));
            Intent intent = new Intent(getContext(),MapsActivity.class);
            intent.putExtra(MapsActivity.MAP_MODE_BUNDLE_NAME,MapsActivity.MAP_TYPE_EXPLORE);
            //startActivityForResult(intent,37);
            mGetContent.launch(intent);

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