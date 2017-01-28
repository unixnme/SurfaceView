package com.blogspot.unixnme.surfaceview;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
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

public class SurfaceViewMain extends AppCompatActivity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback, Camera.ShutterCallback {

    private static final String TAG = SurfaceViewMain.class.getSimpleName();

    private SurfaceViewMain instance;
    private SurfaceView surfaceView;
    private OverlaidView overlaidView;
    private Camera camera;
    private Camera.Size previewSize;
    private SurfaceHolder surfaceHolder;
    private FloatingActionButton flipCameraButton;
    private int width, height;
    private int cameraId;
    private int maxFocusAreas;
    private int maxMeteringAreas;
    private boolean volumeLongPressed;
    private boolean takePictureLock;
    private int currentCameraFacing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.surface_view_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        overlaidView = (OverlaidView) findViewById(R.id.overlaid_view);
        overlaidView.setMainInstance(this);
        flipCameraButton = (FloatingActionButton) findViewById(R.id.switch_camera_FAB);
        flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "flip camera button clicked");
                switchCameraFacingDirection();
            }
        });
    }

    protected void onResume() {
        super.onResume();
        volumeLongPressed = false;
        takePictureLock = false;
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
        surfaceHolder = holder;
        cameraId = getCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
        currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
        camera = Camera.open(cameraId);

        if (camera != null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.canDisableShutterSound)
                camera.enableShutterSound(false);

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
        if (previewSize.width * height != previewSize.height * width) {
            Log.w(TAG, "preview aspect ratio differs from surfaceview");
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surface destroyed");
    }

    private int getCameraId(int facingDirection) throws IllegalArgumentException {
        if (facingDirection != Camera.CameraInfo.CAMERA_FACING_BACK && facingDirection != Camera.CameraInfo.CAMERA_FACING_FRONT)
            throw new IllegalArgumentException("facingDirection invalid for getCameraId");
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == facingDirection) {
                cameraId = i;
                break;
            }
        }

        return cameraId;
    }

    private synchronized void switchCameraFacingDirection() {
        if (camera == null)
            return;

        if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
            currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_FRONT;
        else
            currentCameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;

        camera.stopPreview();
        camera.release();
        int cameraId = getCameraId(currentCameraFacing);

        camera = Camera.open(cameraId);

        if (camera != null) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            if (cameraInfo.canDisableShutterSound)
                camera.enableShutterSound(false);

            try {
                setCameraDisplayOrientation(this, cameraId, camera);
                camera.setPreviewDisplay(surfaceHolder);
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
        if (camera != null && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (takePictureLock)
                // take picture is already in queue; ignore any more take picture requests
                return true;

            takePictureLock = true;
            if (volumeLongPressed) {
                Log.i(TAG, "long press");
                volumeLongPressed = false;
                // take picture in 3 secs
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG, "schedule take pic after long press");
                        camera.takePicture(instance, null, instance);
                    }
                }, 3000);
            }
            else {
                Log.i(TAG, "short press; schedule take pic immediately");
                camera.takePicture(instance, null, this);
            }
            return true;
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
        takePictureLock = false;
    }

    public void onShutter() {
        overlaidView.animateTakePicture();
    }

    // Create an image file name
    private String getFilename() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = getExternalStoragePublicDirectory(DIRECTORY_PICTURES).getPath().toString() + "/" + timeStamp + ".jpg";

        return filename;
    }

}


