package fr.abitbol.service4night.utils;

import java.util.List;

import fr.abitbol.service4night.listeners.OnPicturesUploadedListener;

public interface PicturesUploadAdapter extends OnPicturesUploadedListener {

    public void startProgressBar();
    public void stopProgressBar();
    public void updateProgressBar(boolean success,int done);

}
