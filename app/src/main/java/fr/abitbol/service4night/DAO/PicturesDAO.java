/*
 * Nom de classe : PicturesDAO
 *
 * Description   : classe fournissant un accès aux photographies lieux soit hors ligne (prochainement...), soit sur firebase.
 *
 * Auteur        : Olivier Baylac.
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.DAO;

import android.graphics.Bitmap;

public interface PicturesDAO {
    public void insert(String pictureName, String userId, String locationId, Bitmap picture);
    public Boolean delete(String url);
    public String select(String pictureName,String userId, String locationId);
}
