package com.blogspot.unixnme.takeiteasy;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import org.w3c.dom.Attr;

public class DemoView extends LinearLayout {

    public DemoView(Context context) {
        super(context);
    }

    public DemoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public DemoView(Context context, AttributeSet attributeSet, int defStyleAttr) {
        super(context, attributeSet, defStyleAttr);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
}
