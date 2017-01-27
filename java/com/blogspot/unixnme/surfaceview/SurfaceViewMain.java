package com.blogspot.unixnme.surfaceview;

import android.app.Activity;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SurfaceViewMain extends Activity implements SurfaceHolder.Callback, Camera.AutoFocusCallback {

    private static final String TAG = SurfaceViewMain.class.getSimpleName();
    private static final int FOCUS_WIDTH = 100;
    private static final int FOCUS_HEIGHT = 100;

    private SurfaceView surfaceView;
    private OverlaidView overlaidView;
    private Camera camera;
    private Camera.Size previewSize;
    private int width, height;
    private int cameraId;
    private int maxFocusAreas;
    private int maxMeteringAreas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public void setFocus(int x, int y) {
        if (camera == null || maxFocusAreas <= 0)
            return;

        int left = Math.max(-1000, x - FOCUS_WIDTH/2);
        int right = Math.min(1000, x + FOCUS_WIDTH/2);
        int top = Math.max(-1000, y - FOCUS_HEIGHT/2);
        int bot = Math.min(1000, y + FOCUS_HEIGHT/2);

        Rect focusRect = new Rect(left, top, right, bot);
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
}


