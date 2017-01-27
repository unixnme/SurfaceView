package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by linuxnme on 1/27/17.
 */

public class OverlaidView extends View {
    private static final String TAG = OverlaidView.class.getSimpleName();
    private static final float FOCUS_WIDTH = 100;
    private static final float FOCUS_HEIGHT = 100;

    private Paint paint;
    private float x1,x2,y1,y2;
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
            float x = event.getX();
            float y = event.getY();
            invalidate();

            float width = getWidth();
            float height = getHeight();

            x1 = Math.max(0, x - FOCUS_WIDTH/2);
            x2 = Math.min(width, x + FOCUS_WIDTH/2);
            y1 = Math.max(0, y - FOCUS_HEIGHT/2);
            y2 = Math.min(height, y + FOCUS_HEIGHT/2);


            float left = x1 / width * 2000 - 1000;
            float right = x2 / width * 2000 - 1000;
            float top = y1 / height * 2000 - 1000;
            float bot = y2 / height * 2000 - 1000;

            mainInstance.setFocus(new Rect((int)left, (int)top, (int)right, (int)bot));
        }
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawRect(new Rect((int)x1, (int)y1, (int)x2, (int)y2), paint);
    }
}
