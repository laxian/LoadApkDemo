package com.mumuWeibo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;

import dynamicloader.ReflectionUtils;

public class SecondActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        TextView textView = (TextView) findViewById(R.id.textview);
        textView.setText(AppApplication.instance.getClass().getName());
        Context baseContext = AppApplication.instance.getBaseContext();
        textView.setText(baseContext.getPackageResourcePath());
        try {
            Field mPackageInfo = ReflectionUtils.getField(baseContext, "mPackageInfo");
            String name = mPackageInfo.getName();
            Log.d("second", name);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        findViewById(R.id.bt_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecondActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
