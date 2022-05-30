/*
 * Nom d'interface' : OnItemClickedListener
 *
 * Description   : interface fonctionelle sur le clic sur un des éléments de la liste des lieux d'un utilisateur
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

import fr.abitbol.service4night.MapLocation;

public interface OnItemClickedListener {
    public void onItemClicked(int position, MapLocation mapLocation);
}
