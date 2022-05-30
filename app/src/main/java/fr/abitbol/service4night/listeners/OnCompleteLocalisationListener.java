/*
 * Nom d'interface' : OnCompleteLocalisationListener
 *
 * Description   : interface fonctionelle sur la localisation de l'utilisateur
 *
 * Auteur       : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.listeners;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public interface OnCompleteLocalisationListener  {
    void onCompleteLocation(@NonNull Task<Location> task);

}
