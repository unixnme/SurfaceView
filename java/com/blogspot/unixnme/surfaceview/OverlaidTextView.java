package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class OverlaidTextView extends View {
    private static final String TAG = OverlaidTextView.class.getSimpleName();

    private OverlaidTextView instance;
    private Paint paint;
    private SurfaceViewMain mainInstance;
    private String textToWrite;

    public OverlaidTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        instance = this;
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        textToWrite = "";
    }

    public void setMainInstance(SurfaceViewMain instance) {
        mainInstance = instance;
    }

    public void writeText(String text) {
        textToWrite = text;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawText(textToWrite, getWidth()/2, getHeight()/2, paint);
    }
}
