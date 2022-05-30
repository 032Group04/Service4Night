/*
 * Nom de classe : PictureUploader
 *
 * Description   : implémente l'upload 'de photos via un PictureUploadTask
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


import fr.abitbol.service4night.listeners.OnPicturesUploadedListener;

public interface PicturesUploader extends OnPicturesUploadedListener {

    public void startProgressBar();
    public void stopProgressBar();
    public void updateProgressBar(boolean success,int done);

}
