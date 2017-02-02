package com.blogspot.unixnme.takeiteasy;

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
    private TakeItEasyMain mainInstance;
    private String textToWrite;
    private float gravityAngle;

    public OverlaidTextView(Context context) {
        super(context);
    }

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

    public OverlaidTextView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    void setMainInstance(TakeItEasyMain instance) {
        mainInstance = instance;
    }

    void writeText(String text) {
        textToWrite = text;
        invalidate();
    }

    void setGravityAngle(float gravityAngle) {
        this.gravityAngle = gravityAngle;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float x = getWidth()/2;
        float y = getHeight()/2;
        canvas.rotate(gravityAngle, x, y);
        int textSize;
        if (textToWrite.length() > 1)
            textSize = 100;
        else
            textSize = 300;
        paint.setTextSize(textSize);
        canvas.drawText(textToWrite, x, y, paint);
    }
}
