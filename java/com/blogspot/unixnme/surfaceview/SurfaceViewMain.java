package com.blogspot.unixnme.surfaceview;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;

public class SurfaceViewMain extends Activity implements SurfaceHolder.Callback {

    private static final String TAG = SurfaceViewMain.class.getSimpleName();
    private SurfaceView surfaceView;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.surface_view_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view_main);
        surfaceView.getHolder().addCallback(this);
    }

    protected void onResume() {
        super.onResume();
        if (camera != null)
            camera.startPreview();
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
        camera = Camera.open();
        if (camera != null) {
            camera.setDisplayOrientation(90);
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException ie) {

            }
        }
        if (camera != null)
            camera.startPreview();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surface destroyed");
    }
}
