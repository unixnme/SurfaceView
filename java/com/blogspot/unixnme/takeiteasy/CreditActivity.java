package com.blogspot.unixnme.takeiteasy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class CreditActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.credit_layout);
        TextView switch_camera_link = (TextView) findViewById(R.id.switch_camera_icon_text);
        switch_camera_link.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
