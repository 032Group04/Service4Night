package fr.abitbol.service4night.DAO;

import android.graphics.Bitmap;
import android.net.Uri;

import fr.abitbol.service4night.listeners.OnPictureDownloadListener;

public interface PicturesDAO {
    public void insert(String pictureName, String userId, String locationId, Bitmap picture);
    public boolean delete(String url);
    public String select(String pictureName,String userId);
    public String update(String url,String pictureName, String userId, String locationId, Bitmap picture);
}
