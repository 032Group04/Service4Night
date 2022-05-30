/*
 * Nom d'interface' : OnPictureDownloadListener
 *
 * Description   : interface fonctionelle sur le téléchargement d'une photo
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

import java.util.ArrayList;

import fr.abitbol.service4night.pictures.SliderItem;

public interface OnPictureDownloadListener {


    public void onPictureDownload(ArrayList<SliderItem> _images);


}
