package com.blogspot.unixnme.surfaceview;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceViewMain extends Activity implements SurfaceHolder.Callback {

    public final String TAG = "SurfaceActivity";
    private SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.surface_view_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_view_main);
        surfaceView.getHolder().addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.i(TAG, "surface created");
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i(TAG, "surface changed");
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "surface destroyed");
    }
}
