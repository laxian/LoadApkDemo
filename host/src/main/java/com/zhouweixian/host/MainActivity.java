package com.zhouweixian.host;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhouweixian.host.hook.HookHelper;
import com.zhouweixian.host.util.Utils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;


@SuppressWarnings("all")
public class MainActivity extends Activity implements View.OnClickListener {

    private String apkDir = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static final String apkName = "guestapk-debug.apk";
    private static final String pkgName = "com.zhouweixian.guest";
    private Button button;
    private Button button2;
    private Button button3;
    private DexClassLoader dexClassLoader;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Resources resources;
    private TextView output;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        HookHelper.hookActivityManagerNative(this);
        HookHelper.hookActivityThreadHandler();
        String apkFullPath = apkDir + File.separator + apkName;
        resources = Utils.getPluginResources(apkFullPath, this.getResources());
        dexClassLoader = Utils.getDexClassLoader(apkFullPath, getDir("dex", Context.MODE_PRIVATE).getPath());
    }

    private void initView() {
        imageView = (ImageView) findViewById(R.id.icon);
        output = (TextView) findViewById(R.id.et_output);
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
                Class<?> hotfixActivity = null;
                try {
                    hotfixActivity = Utils.getClassFromApk(dexClassLoader, pkgName, "HotFixActivity");
                    Utils.addPathToDexPathList(this, dexClassLoader);
                    Intent intent = new Intent(this, hotfixActivity);
                    intent.setClassName(pkgName, hotfixActivity.getName());
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button2:
                // 从apk中加载Activity
                Class<?> codeUIActivity = null;
                try {
                    codeUIActivity = Utils.getClassFromApk(dexClassLoader, pkgName, "CodeUIActivity");
                    Utils.addPathToDexPathList(this, dexClassLoader);
                    Intent intent = new Intent(this, codeUIActivity);
                    intent.setClassName(pkgName, codeUIActivity.getName());
                    startActivity(intent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button3:
                startActivity(new Intent(MainActivity.this, RealActivity.class));
                break;
            case R.id.button4:
                // 从apk中读取string 资源
                int txtId = 0;
                try {
                    txtId = Utils.getResIdFromApk(dexClassLoader, pkgName, "string", "guest_text");
                    String txt;
                    if (resources != null) {
                        txt = resources.getString(txtId);
                        output.setText(txt);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button5:
                // 从apk 中读取drawable 资源
                int imgId = 0;
                try {
                    imgId = Utils.getResIdFromApk(dexClassLoader, pkgName, "drawable", "guest_img");
                    Drawable drawable;
                    if (resources != null) {
                        drawable = resources.getDrawable(imgId);
                        imageView.setImageDrawable(drawable);
                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button6:

                // 从apk中加载类
                Class<?> demoClass = null;
                try {
                    demoClass = Utils.getClassFromApk(dexClassLoader, pkgName, "Demo");
                    // 反射调用对象方法
                    Method func1 = demoClass.getMethod("func1");
                    String func1Msg = (String) func1.invoke(demoClass.newInstance());
                    output.setText(func1Msg);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button7:
                try {
                    demoClass = Utils.getClassFromApk(dexClassLoader, pkgName, "Demo");
                    // 反射调用类方法
                    Method func2 = demoClass.getMethod("func2");
                    String func2Msg = (String) func2.invoke(demoClass);
                    output.setText(func2Msg);
                    Log.d("abc", demoClass.toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
}
