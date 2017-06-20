package com.zhouweixian.host;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zhouweixian.host.hook.AmsHookHelper;
import com.zhouweixian.host.hook.ClHookHelper;
import com.zhouweixian.host.util.Utils;

import java.lang.reflect.InvocationTargetException;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String apkName = "guestapk.apk";
    private static final String pkgName = "com.zhouweixian.guest";
    private Button button;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(this);
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(this);
        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(this);
        button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(this);
        button7 = (Button) findViewById(R.id.button7);
        button7.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                // 从apk中加载Activity
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(pkgName, pkgName + ".MainActivity"));
                startActivity(intent);
                break;
            case R.id.button2:
                startActivity(new Intent(this, DummyActivity.class));
                break;
            case R.id.button3:
                break;
            case R.id.button4:
                break;
            case R.id.button5:
                break;
            case R.id.button6:
                break;
            case R.id.button7:
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        Utils.extractAssets(this, "guestapk.apk");


        try {
            ClHookHelper.addPackage(getFileStreamPath(apkName));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        AmsHookHelper.hookActivityManagerNative(this);
        AmsHookHelper.hookActivityThreadHandler();


    }
}
