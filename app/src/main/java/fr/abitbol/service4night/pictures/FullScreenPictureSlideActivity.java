/*
 * Nom de classe : FullScreenPictureSlideActivity
 *
 * Description   : activité affichant le viewPager en plein écran
 *
 * Auteur        : Olivier Baylac
 *
 * Version       : 1.0
 *
 * Date          : 28/05/2022
 *
 * Copyright     : CC-BY-SA
 */
package fr.abitbol.service4night.pictures;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.util.ArrayList;

import fr.abitbol.service4night.databinding.ActivityFullScreenPictureSlideBinding;
import fr.abitbol.service4night.fragments.LocationFragment;
import fr.abitbol.service4night.pictures.SliderAdapter;
import fr.abitbol.service4night.pictures.SliderItem;
import fr.abitbol.service4night.pictures.TouchSliderAdapter;



/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullScreenPictureSlideActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final String PICTURES_PATHS_LIST_NAME = "picturesPaths";
    private static final String PICTURES_NAMES_LIST_NAME = "picturesNames";
    private static final int UI_ANIMATION_DELAY = 300;
    private static final String TAG = "FullScreePictureSlideActivity logging";
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private ViewPager2 viewPager;
    private static int lastOrientation = -1;
    private OrientationEventListener orientationListener;
    private ArrayList<String> paths;
    private ArrayList<String> names;
    private ArrayList<SliderItem> images;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            if (Build.VERSION.SDK_INT >= 30) {
                mContentView.getWindowInsetsController().hide(
                        WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            } else {
                // Note that some of these constants are new as of API 16 (Jelly Bean)
                // and API 19 (KitKat). It is safe to use them, as they are inlined
                // at compile-time and do nothing on earlier devices.
                mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.i(TAG, "onTouch called");
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (AUTO_HIDE) {
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    private ActivityFullScreenPictureSlideBinding binding;

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called");
        if (orientationListener != null){
            Log.i(TAG, "onPause: disabling orientation listener");
            orientationListener.disable();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");
        if (orientationListener != null){
            Log.i(TAG, "onResume: re-enabling orientation listener");
            orientationListener.enable();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart called");
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (names != null){
            outState.putStringArrayList(PICTURES_NAMES_LIST_NAME,names);
        }
        if (paths != null){
            outState.putStringArrayList(PICTURES_PATHS_LIST_NAME,paths);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate called");
        binding = ActivityFullScreenPictureSlideBinding.inflate(getLayoutInflater());
        if (getIntent() != null && getIntent().getExtras()!=null){
            Log.i(TAG, "onCreate: intent and extras are valid");
            Bundle extras = getIntent().getExtras();
            paths = extras.getStringArrayList(LocationFragment.EXTRA_PICTURES_PATHS);
            names = extras.getStringArrayList(LocationFragment.EXTRA_PICTURES_NAMES);
        }
        else Log.i(TAG, "onCreate: intent or extra is null");
        if (savedInstanceState != null){
            if (savedInstanceState.containsKey(PICTURES_PATHS_LIST_NAME)){
                paths = savedInstanceState.getStringArrayList(PICTURES_PATHS_LIST_NAME);
            }
            if (savedInstanceState.containsKey(PICTURES_NAMES_LIST_NAME)){
                names = savedInstanceState.getStringArrayList(PICTURES_NAMES_LIST_NAME);
            }
        }
        viewPager = binding.fullscreenViewPager;

        images = new ArrayList<>();
        for (int i = 0;i < paths.size();i++){
            Bitmap bitmap = BitmapFactory.decodeFile(paths.get(i));
            if (bitmap != null){
                Log.i(TAG, "onCreate: bitmap "+ names.get(i) + "successfully created");
                images.add(new SliderItem(bitmap,names.get(i)));

            }
            else Log.i(TAG, "onCreate: bitmap "+ names.get(i) + " creation failed");
        }

        viewPager.setAdapter(new TouchSliderAdapter(images,viewPager));


        setContentView(binding.getRoot());

        mVisible = true;

        mContentView = binding.fullScreenContent;

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //binding.dummyButton.setOnTouchListener(mDelayHideTouchListener);
    }



    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i(TAG, "onPostCreate: called");
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int i) {
//                Log.i(TAG, "onOrientationChanged called");
                Display display = windowManager.getDefaultDisplay();
                int rotation = display.getRotation();
                if (rotation != lastOrientation){
                    Log.i(TAG, "onOrientationChanged: orientation changed");
                    viewPager.setAdapter(new TouchSliderAdapter(images,viewPager));
                    viewPager.invalidate();
                }
                lastOrientation = rotation;
            }
        };
        if (orientationListener.canDetectOrientation()){
            Log.i(TAG, "onCreate: listener can detect orientation");
            orientationListener.enable();
        }
        else{
            Log.i(TAG, "onCreate: listener can't detect orientation");
        }
        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private void show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            mContentView.getWindowInsetsController().show(
                    WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}