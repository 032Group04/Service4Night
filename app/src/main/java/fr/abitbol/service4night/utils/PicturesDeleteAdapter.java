package fr.abitbol.service4night.utils;

import fr.abitbol.service4night.listeners.OnPictureDeleteListener;

public interface PicturesDeleteAdapter extends OnPictureDeleteListener {
    public void startDeleteBar();
    public void stopDeleteBar();
    public void updateDeleteBar(boolean success,int done);
}
