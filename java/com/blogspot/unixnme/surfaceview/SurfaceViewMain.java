package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class SurfaceViewMain extends AppCompatActivity implements SurfaceHolder.Callback, Camera.AutoFocusCallback, Camera.PictureCallback, Camera.ShutterCallback, SensorEventListener {

    private static final String TAG = SurfaceViewMain.class.getSimpleName();

    private SurfaceViewMain instance;
    private SurfaceView surfaceView;
    private OverlaidView overlaidView;
    private OverlaidTextView overlaidTextView;
    private Camera camera;
    private Camera.Size previewSize;
    private SurfaceHolder surfaceHolder;
    private FloatingActionButton flipCameraButton;
    private FrameLayout frameLayout;
    private FloatingActionButton captureButton;
    private int surfaceWidth, surfaceHeight, pxWidth, pxHeight;
    private float dpWidth, dpHeight;
    private int cameraId;
    private int maxFocusAreas;
    private int maxMeteringAreas;
    private boolean takePictureLock;
    private int currentCameraFacing;
    private SensorManager sensorManager;
    private Sensor gSensor;
    protected float gravityX, gravityY, gravityAngle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        instance = this;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.surface_view_main);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pxWidth = size.x;
        pxHeight = size.y;
        DisplayMetrics displayMetrics = this.getResources().getDisplayMetrics();
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(this);
        overlaidView = (OverlaidView) findViewById(R.id.overlaid_view);
        overlaidView.setMainInstance(this);
        overlaidTextView = (OverlaidTextView) findViewById(R.id.countdown_textview);
        overlaidTextView.setMainInstance(this);
        flipCameraButton = (FloatingActionButton) findViewById(R.id.switch_camera_FAB);
        flipCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "flip camera button clicked");
                switchCameraFacingDirection();
            }
        });
        frameLayout = (FrameLayout) findViewById(R.id.image_button_frame_layout);
        captureButton = (FloatingActionButton) findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePictureWithCorrectOrientation(instance, null, instance);
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    }

    protected void onResume() {
        super.onResume();
        takePictureLock = false;
        sensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        if (camera != null) {
            camera.stopPreview();
            camera.release();
        }
        sensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        gravityX = event.values[0];
        gravityY = event.values[1];

        gravityAngle = (float) (Math.atan2(-gravityY, gravityX) * 180 / Math.PI);

        ViewCompat.animate(flipCameraButton).rotation(gravityAngle).setDuration(0).start();
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
        this.surfaceWidth = width;
        this.surfaceHeight = height;
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

    protected void setFocus(Rect focusRect) {
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
            if (!takePictureLock) {
                event.startTracking();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private synchronized void takePictureWithCorrectOrientation(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
        if (camera == null)
            return;

        int angle = 0;
        if (-180 <= gravityAngle && gravityAngle < -135)
            angle = 180;
        else if (-135 <= gravityAngle && gravityAngle < -45) {
            if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
                angle = 90;
            else
                angle = 270;
        }
        else if (-45 <= gravityAngle && gravityAngle < 45)
            angle = 0;
        else if (45 <= gravityAngle && gravityAngle < 135) {
            if (currentCameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK)
                angle = 270;
            else
                angle = 90;
        }
        else if (135 <= gravityAngle && gravityAngle <= 180)
            angle = 180;

        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(angle);
        camera.setParameters(parameters);
        camera.takePicture(shutter, raw, jpeg);
    }


    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event){
        if (camera != null && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            if (takePictureLock)
                // skip it if already take picture in the queue
                return true;

            takePictureLock = true;
            Log.i(TAG, "long press");

            // take picture in 3 secs
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "schedule take pic after long press");
                    takePictureWithCorrectOrientation(instance, null, instance);
                }
            }, 3000);
            new CountDownTimer(3000, 1000) {
                public void onTick(long ms) {
                    String text = String.valueOf(Math.round((float)ms/1000));
                    Log.d(TAG, "countdown timer: " + ms);
                    overlaidTextView.writeText(text);
                }

                public void onFinish() {
                    overlaidTextView.writeText("");
                }
            }.start();
            return true;
        }
        return onKeyLongPress(keyCode,event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (camera != null && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {

            if (takePictureLock)
                return true;

            takePictureLock = true;
            Log.i(TAG, "short press; schedule take pic in 0.5sec");
            overlaidTextView.writeText("Cheese~");
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "schedule take pic after short press release");
                    takePictureWithCorrectOrientation(instance, null, instance);
                    overlaidTextView.writeText("");
                }
            }, 500);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    public void onPictureTaken(byte[] data, Camera camera) {
        String filename = getFilename();
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            fos.write(data);
            fos.close();
        } catch (IOException e) {

        }
        addToGallery(filename);
        takePictureLock = false;
        camera.startPreview();
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

    private void addToGallery(String filename) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filename);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

}


