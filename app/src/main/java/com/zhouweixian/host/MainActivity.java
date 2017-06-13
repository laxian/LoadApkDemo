package com.zhouweixian.host;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;


@SuppressWarnings("all")
public class MainActivity extends Activity {

    private String apkDir = Environment.getExternalStorageDirectory().getPath() + File.separator;
    private static final String apkName = "guestapk-debug.apk";
    private static final String pkgName = "com.zhouweixian.guest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView imageView = (ImageView) findViewById(R.id.icon);
        TextView textView = (TextView) findViewById(R.id.text);
        TextView editText = (TextView) findViewById(R.id.et_output);

        String apkFullPath = apkDir + File.separator + apkName;
        Resources resources = getPluginResources(apkFullPath);
        DexClassLoader dexClassLoader = getDexClassLoader(apkFullPath);

        try {
            // 从apk 中读取drawable 资源
            int imgId = getResIdFromApk(dexClassLoader, pkgName, "drawable", "guest_img");
            Drawable drawable;
            if (resources != null) {
                drawable = resources.getDrawable(imgId);
                imageView.setImageDrawable(drawable);
            }

            // 从apk中读取string 资源
            int txtId = getResIdFromApk(dexClassLoader, pkgName, "string", "guest_text");
            String txt;
            if (resources != null) {
                txt = resources.getString(txtId);
                textView.setText(txt);
            }

            // 从apk中加载类
            Class<?> demoClass = getClassFromApk(dexClassLoader, pkgName, "Demo");

            // 反射调用对象方法
            Method func1 = demoClass.getMethod("func1");
            String func1Msg = (String) func1.invoke(demoClass.newInstance());
            editText.setText(func1Msg);

            // 反射调用类方法
            Method func2 = demoClass.getMethod("func2");
            String func2Msg = (String) func2.invoke(demoClass);
            editText.setText(String.format("func1 -> %s \nfunc2 -> %s", editText.getText(), func2Msg));
            Log.d("abc", demoClass.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 加载apk获得内部资源
     *
     * @param apkPackageName apk文件的包名
     * @param resType        (anim, attr,bool,color,dimen,drawable,id,integer,layout,mipmap,string,style,styleable)
     * @param resName        带查找的资源名
     */
    public int getResIdFromApk(DexClassLoader dexClassLoader, String apkPackageName, String resType, String resName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        //通过使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id
        Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$" + resType);

        Field field = clazz.getDeclaredField(resName);
        //得到资源id
        return field.getInt(R.id.class);
    }

    @NonNull
    private DexClassLoader getDexClassLoader(String apkFullPath) {
        //在应用安装目录下创建一个名为app_dex文件夹目录,如果已经存在则不创建
        File optimizedDirectoryFile = getDir("dex", Context.MODE_PRIVATE);
        //参数：
        // 1、包含dex的apk文件或jar文件的路径;
        // 2、apk、jar解压缩生成dex存储的目录;
        // 3、本地library库目录，一般为null;
        // 4、父ClassLoader;
        return new DexClassLoader(
                apkFullPath,
                optimizedDirectoryFile.getPath(),
                null,
                ClassLoader.getSystemClassLoader());
    }


    /**
     * 加载apk，并返回{className}指定的类
     *
     * @param apkPackageName apk文件的包名
     * @param className      类名
     * @return className对应的类
     */
    public Class<?> getClassFromApk(DexClassLoader dexClassLoader, String apkPackageName, String className) throws ClassNotFoundException {

        //通过使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id

        return dexClassLoader.loadClass(apkPackageName + "." + className);
    }

    /**
     * @return 得到对应插件的Resource对象
     */
    private Resources getPluginResources(String apkFullPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            //反射调用方法addAssetPath(String path)
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            //将未安装的Apk文件的添加进AssetManager中，第二个参数为apk文件的路径带apk名
            addAssetPath.invoke(assetManager, apkFullPath);
            Resources superRes = this.getResources();
            return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
