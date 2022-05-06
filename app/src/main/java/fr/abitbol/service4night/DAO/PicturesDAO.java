package fr.abitbol.service4night.DAO;

import android.graphics.Bitmap;

public interface PicturesDAO {
    public void insert(String path, String userId, String locationId, Bitmap picture);
}
