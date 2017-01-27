package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.EventLog;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by linuxnme on 1/27/17.
 */

public class OverlaidView extends View {
    private static final String TAG = OverlaidView.class.getSimpleName();

    private Paint paint;
    private float x,y;
    private SurfaceViewMain mainInstance;

    public OverlaidView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setMainInstance(SurfaceViewMain instance) {
        mainInstance = instance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x = event.getX();
            y = event.getY();
            invalidate();
            float focusX = x/(float)getWidth() * 2000 - 1000;
            float focusY = y/(float)getHeight() * 2000 - 1000;
            mainInstance.setFocus((int)focusX, (int)focusY);
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawCircle(x, y, 10, paint);
    }
}
