/*
 * Nom d'interface' : OnInfoWindowClickedAdapter
 *
 * Description   : interface fonctionelle sur le clic infowindow
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

public interface OnInfoWindowClickedAdapter {
    public void infoWindowClicked(MapLocation location);


}
