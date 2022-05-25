package fr.abitbol.service4night.DAO;

import android.graphics.Bitmap;

public interface PicturesDAO {
    public void insert(String pictureName, String userId, String locationId, Bitmap picture);
    public Boolean delete(String url);
    public String select(String pictureName,String userId, String locationId);
    public String update(String url,String pictureName, String userId, String locationId, Bitmap picture);
}
