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

import com.zhouweixian.host.util.ReflectUtil;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;


@SuppressWarnings("all")
public class MainActivity extends Activity implements View.OnClickListener {

    private String apkDir = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static final String apkName = "guestapk-debug.apk";
    private static final String pkgName = "com.zhouweixian.guest";
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        ImageView imageView = (ImageView) findViewById(R.id.icon);
        TextView textView = (TextView) findViewById(R.id.text);
        TextView editText = (TextView) findViewById(R.id.et_output);

        String apkFullPath = apkDir + File.separator + apkName;
        Resources resources = ReflectUtil.getPluginResources(apkFullPath, this.getResources());
        DexClassLoader dexClassLoader = ReflectUtil.getDexClassLoader(apkFullPath, getDir("dex", Context.MODE_PRIVATE).getPath());

        try {
            // 从apk 中读取drawable 资源
            int imgId = ReflectUtil.getResIdFromApk(dexClassLoader, pkgName, "drawable", "guest_img");
            Drawable drawable;
            if (resources != null) {
                drawable = resources.getDrawable(imgId);
                imageView.setImageDrawable(drawable);
            }

            // 从apk中读取string 资源
            int txtId = ReflectUtil.getResIdFromApk(dexClassLoader, pkgName, "string", "guest_text");
            String txt;
            if (resources != null) {
                txt = resources.getString(txtId);
                textView.setText(txt);
            }

            // 从apk中加载类
            Class<?> demoClass = ReflectUtil.getClassFromApk(dexClassLoader, pkgName, "Demo");

            // 反射调用对象方法
            Method func1 = demoClass.getMethod("func1");
            String func1Msg = (String) func1.invoke(demoClass.newInstance());
            editText.setText(func1Msg);

            // 反射调用类方法
            Method func2 = demoClass.getMethod("func2");
            String func2Msg = (String) func2.invoke(demoClass);
            editText.setText(String.format("func1 -> %s \nfunc2 -> %s", editText.getText(), func2Msg));
            Log.d("abc", demoClass.toString());

            // 从apk中加载Activity
//            Class<?> guestMainActivityClass = ReflectUtil.getClassFromApk(dexClassLoader, pkgName, "MainActivity");
//            HookHelper.hookActivityManagerNative();
//            HookHelper.hookActivityThreadHandler();
//            ReflectUtil.addPathToDexPathList(this, dexClassLoader);
//            Intent intent = new Intent(this, guestMainActivityClass);
//            intent.setClassName("com.zhouweixian.guest", guestMainActivityClass.getName());
//            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        button = (Button) findViewById(R.id.button);

        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
                break;
        }
    }
}
