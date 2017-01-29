package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
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
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextAlign(Paint.Align.CENTER);
        textToWrite = "";
    }

    public void setMainInstance(SurfaceViewMain instance) {
        mainInstance = instance;
    }

    protected void writeText(String text) {
        textToWrite = text;
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        float x = getWidth()/2;
        float y = getHeight()/2;
        canvas.rotate(mainInstance.gravityAngle, x, y);
        int textSize;
        if (textToWrite.length() > 1)
            textSize = 100;
        else
            textSize = 300;
        paint.setTextSize(textSize);
        canvas.drawText(textToWrite, x, y, paint);
    }
}
