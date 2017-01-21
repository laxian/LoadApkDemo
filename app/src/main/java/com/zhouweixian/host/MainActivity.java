package com.zhouweixian.host;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {


    private String apkDir = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static final String apkName = "guestapk-debug.apk";
    private static final String pkgName = "com.zhouweixian.guest";
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.icon);
        TextView textView = (TextView) findViewById(R.id.text);

        resources = getPluginResources(apkName);
        try {
            int imgId = getResIdFromApk(apkDir, apkName, pkgName, "drawable", "guest_img");
            Drawable drawable = resources.getDrawable(imgId);
            imageView.setImageDrawable(drawable);

            int txtId = getResIdFromApk(apkDir, apkName, pkgName, "string", "guest_text");
            String txt = resources.getString(txtId);
            textView.setText(txt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载apk获得内部资源
     *
     * @param apkDir         存放apk的目录
     * @param apkName        apk文件名
     * @param apkPackageName apk文件的包名
     * @param resType        (anim, attr,bool,color,dimen,drawable,id,integer,layout,mipmap,string,style,styleable)
     * @param resName        带查找的资源名
     * @throws ClassNotFoundException
     * @throws NoSuchFieldException
     * @throws IllegalAccessException
     */
    private int getResIdFromApk(String apkDir, String apkName, String apkPackageName, String resType, String resName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        //在应用安装目录下创建一个名为app_dex文件夹目录,如果已经存在则不创建
        File optimizedDirectoryFile = getDir("dex", Context.MODE_PRIVATE);
        //参数：
        // 1、包含dex的apk文件或jar文件的路径;
        // 2、apk、jar解压缩生成dex存储的目录;
        // 3、本地library库目录，一般为null;
        // 4、父ClassLoader;
        DexClassLoader dexClassLoader = new DexClassLoader(
                apkDir + File.separator + apkName,
                optimizedDirectoryFile.getPath(),
                null,
                ClassLoader.getSystemClassLoader());

        //通过使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id
        Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$" + resType);

        Field field = clazz.getDeclaredField(resName);
        //得到资源id
        return field.getInt(R.id.class);
    }

    /**
     * @param apkName
     * @return 得到对应插件的Resource对象
     */
    private Resources getPluginResources(String apkName) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            //反射调用方法addAssetPath(String path)
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            //将未安装的Apk文件的添加进AssetManager中，第二个参数为apk文件的路径带apk名
            addAssetPath.invoke(assetManager, apkDir + File.separator + apkName);
            Resources superRes = this.getResources();
            return new Resources(assetManager, superRes.getDisplayMetrics(),
                    superRes.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
