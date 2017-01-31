package com.blogspot.unixnme.takeiteasy;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

public class DemoActivity extends AppCompatActivity {

    DemoActivity instance;
    private CheckBox checkBox;
    private Button closeButton;
    private View.OnClickListener onClickListener;

    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.demo_activity);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        closeButton = (Button) findViewById(R.id.close_button);
        onClickListener = new View.OnClickListener() {
            public void onClick(View v) {
                if (checkBox.isChecked())
                    TakeItEasyMain.getInstance().setDemoPref(false);
                finish();
            }
        };
        closeButton.setOnClickListener(onClickListener);
    }
}
