package com.blogspot.unixnme.surfaceview;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class SurfaceViewMain extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback {

    private static final String TAG = SurfaceViewMain.class.getSimpleName();

    private SurfaceViewMain instance;
    private SurfaceView surfaceView;
    private OverlaidView overlaidView;
    private Camera camera;
    private Camera.Size previewSize;
    private int width, height;
    private int cameraId;
    private int maxFocusAreas;
    private int maxMeteringAreas;
    private boolean volumeLongPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.surface_view_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        overlaidView = (OverlaidView) findViewById(R.id.overlaid_view);
        overlaidView.setMainInstance(this);
    }

    protected void onResume() {
        super.onResume();
        volumeLongPressed = false;
    }

    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surface created");
        cameraId = getCameraId();
        camera = Camera.open(cameraId);
        if (camera != null) {
            try {
                setCameraDisplayOrientation(this, cameraId, camera);
                camera.setPreviewDisplay(holder);
                Camera.Parameters parameters = camera.getParameters();
                previewSize = parameters.getPreviewSize();
                maxFocusAreas = parameters.getMaxNumFocusAreas();
                maxMeteringAreas = parameters.getMaxNumMeteringAreas();
                camera.startPreview();
            } catch (IOException ie) {
                Log.e(TAG, "setPreviewDisplay fails");
            }
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");
        this.width = width;
        this.height = height;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surface destroyed");
    }

    private int getCameraId() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    // this function is straight from Android Developers Website at
    // https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void setFocus(Rect focusRect) {
        if (camera == null || maxFocusAreas <= 0)
            return;

        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Area> focusArea = new ArrayList<>();
        focusArea.add(new Camera.Area(focusRect, 1000));
        parameters.setFocusAreas(focusArea);
        if (maxMeteringAreas >= 1)
            parameters.setMeteringAreas(focusArea);

        camera.setParameters(parameters);
        camera.autoFocus(this);
    }

    public void onAutoFocus(boolean success, Camera camera) {
        if (success)
            camera.cancelAutoFocus();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            volumeLongPressed = true;
            return true;
        }
        return onKeyLongPress(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (volumeLongPressed) {
                Log.i(TAG, "long press");
                volumeLongPressed = false;
                // take picture in 3 secs
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "schedule take pic after long press");
                        camera.takePicture(null, null, instance);
                    }
                }, 3000);
                return false;
            }

            Log.i(TAG, "short press; schedule take pic immediately");
            camera.takePicture(null, null, this);
        }
        return super.onKeyUp(keyCode, event);
    }

    public void onPictureTaken(byte[] data, Camera camera) {
        try {
            FileOutputStream fos = new FileOutputStream(getFilename());
            fos.write(data);
            fos.close();
        } catch (IOException e) {

        }
    }

    // Create an image file name
    private String getFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath().toString() + "/" + timeStamp + ".jpg";

        return filename;
    }

}


