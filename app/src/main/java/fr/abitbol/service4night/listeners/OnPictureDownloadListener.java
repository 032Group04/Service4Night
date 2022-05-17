package fr.abitbol.service4night.listeners;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;

import java.io.File;
import java.util.ArrayList;

import fr.abitbol.service4night.utils.SliderItem;

public interface OnPictureDownloadListener {


    public void onPictureDownload(ArrayList<SliderItem> _images);


}
