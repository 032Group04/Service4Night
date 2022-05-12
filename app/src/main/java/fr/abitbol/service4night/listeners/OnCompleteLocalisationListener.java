package fr.abitbol.service4night.listeners;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public interface OnCompleteLocalisationListener  {
    void onCompleteLocation(@NonNull Task<Location> task);

}
