/*
 * Nom de classe : UserLocationsFragment
 *
 * Description   : fragment affichant les lieux d'un utilisateur
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

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.abitbol.service4night.utils.DatabaseService;
import fr.abitbol.service4night.MapLocation;
import fr.abitbol.service4night.utils.MapLocationFilter;
import fr.abitbol.service4night.R;
import fr.abitbol.service4night.databinding.FragmentUserLocationsListBinding;
import fr.abitbol.service4night.fragments.LocationUpdateFragment;
import fr.abitbol.service4night.listeners.OnItemClickedListener;

/**
 * A fragment representing a list of Items.
 */
public class UserLocationsFragment extends Fragment implements OnCompleteListener<QuerySnapshot>, OnItemClickedListener {


    private static final String TAG = "UserLocationsFragment logging";
    private FragmentUserLocationsListBinding binding;
    private FirebaseUser user;

    public UserLocationsFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView called ");
        user = FirebaseAuth.getInstance().getCurrentUser();
        binding = FragmentUserLocationsListBinding.inflate(inflater,container,false);
//        View view = inflater.inflate(R.layout.fragment_user_locations_list, container, false);



        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DatabaseService.startService(getContext(),this);
        // Set the adapter


        binding.getRoot().setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext()));

    }

    @Override
    public void onComplete(@NonNull Task<QuerySnapshot> task) {
        if (task.isSuccessful()){
            Log.i(TAG, "onComplete: task successfull");
            List<MapLocation> mapLocations  = new ArrayList<>();
            for (QueryDocumentSnapshot doc : task.getResult()){

                Log.i(TAG, "id : "+doc.getId() + "\ndata : "+ doc.getData());
                mapLocations.add(MapLocation.Builder.build(doc.getData()));
            }
            mapLocations = MapLocationFilter.filterByUser(user.getUid(), mapLocations);
            Log.i(TAG, "onComplete: filtered locations size : "+mapLocations.size());
            binding.getRoot().setAdapter(new MyItemRecyclerViewAdapter(mapLocations,this));
            
        }
        else{
            Log.i(TAG, "onComplete: error while getting document");
        }
    }

    @Override
    public void onItemClicked(int position, MapLocation mapLocation) {
        Log.i(TAG, "onItemClicked: called");
        Bundle bundle = new Bundle();
        bundle.putParcelable(LocationUpdateFragment.MAPLOCATION_NAME,mapLocation);
        NavHostFragment.findNavController(UserLocationsFragment.this).navigate(R.id.action_userLocationsFragment_to_locationUpdateFragment,bundle);
    }
}