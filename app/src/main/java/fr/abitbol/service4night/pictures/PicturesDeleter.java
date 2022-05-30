/*
 * Nom de classe : PictureDeleter
 *
 * Description   : implémente la suppression de photos via un PictureDeleteTask
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.pictures;

import fr.abitbol.service4night.listeners.OnPictureDeleteListener;

public interface PicturesDeleter extends OnPictureDeleteListener {
    public void startDeleteBar();
    public void stopDeleteBar();
    public void updateDeleteBar(boolean success,int done);
}
