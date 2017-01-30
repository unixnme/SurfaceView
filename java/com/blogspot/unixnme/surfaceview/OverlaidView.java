package com.blogspot.unixnme.surfaceview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;

public class OverlaidView extends View {
    private static final String TAG = OverlaidView.class.getSimpleName();
    private static final float FOCUS_WIDTH = 100;
    private static final float FOCUS_HEIGHT = 100;

    private OverlaidView instance;
    private Paint paint;
    private float x1,x2,y1,y2;
    private SurfaceViewMain mainInstance;
    private CountDownTimer countDownTimer;
    private boolean drawRect;
    boolean autoFocusSupported;

    public OverlaidView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        instance = this;
        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        drawRect = false;
        countDownTimer = new CountDownTimer(1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // nothing to do
            }

            @Override
            public void onFinish() {
                invalidate();
            }
        };
    }

    void setMainInstance(SurfaceViewMain instance) {
        mainInstance = instance;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            drawRect = true;
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
    public synchronized void onDraw(Canvas canvas) {
        if (drawRect && autoFocusSupported) {
            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), paint);
            countDownTimer.cancel();
            countDownTimer.start();
        }
        drawRect = false;
    }

    // animate take picture animation
    void animateTakePicture() {
        final long animationDuration = 100;
        final float animationAlpha = 0.5f;
        AlphaAnimation fadeIn = new AlphaAnimation(0, animationAlpha);
        fadeIn.setDuration(animationDuration);
        AlphaAnimation fadeOut = new AlphaAnimation(animationAlpha, 0);
        fadeOut.setDuration(animationDuration);
        fadeOut.setStartOffset(animationDuration);

        //animation.setBackgroundColor(Color.WHITE);
        Log.i(TAG, "starting animation");

        AnimationSet animation = new AnimationSet(false);
        animation.addAnimation(fadeIn);
        animation.addAnimation(fadeOut);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                instance.setBackgroundColor(Color.WHITE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                instance.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        this.startAnimation(animation);
    }
}
