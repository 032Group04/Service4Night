/*
 * Nom d'interface' : OnPictureUploadListener
 *
 * Description   : interface fonctionelle sur l'upload' d'une photo
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

import android.net.Uri;

import java.util.List;

public interface OnPicturesUploadedListener {
    public void onPicturesUploaded(List<String> uris);

}
