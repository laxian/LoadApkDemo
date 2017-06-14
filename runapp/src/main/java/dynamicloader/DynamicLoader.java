package dynamicloader;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import dalvik.system.DexClassLoader;

/**
 * Created by luliang on 5/23/15.
 */
public class DynamicLoader {

    private static final String TAG = DynamicLoader.class.getSimpleName();
    private static final String DEXPATH = "dex_path";
    private static final String ODEX_PATH = "odex_path";

    private String assetApkFilename;
    private DexClassLoader mClassLoader;
    private Context mContxt;
    private String mApkName; //asset中存放的apk文件名，e.g. mumu.apk
    private String mApplicationName; //将要加载的apk的application名字
    private Application mApplication;


    public DynamicLoader(Context ctx, Application hostApplication, String apkName, String
            applicationName) {
        Log.d(TAG, "DynamicLoader init");
        mContxt = ctx;
        mApplication = hostApplication;
        mApkName = apkName;
        if (TextUtils.isEmpty(applicationName)) {
            mApplicationName = Application.class.getName(); //默认
        } else {
            mApplicationName = applicationName;
        }
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "e:  " + e);

        }

    }

    private void init() throws IOException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException, InstantiationException,
            ClassNotFoundException {
        initDexFile();
        initClassLoader();
        replaceFileds();

    }

    /**
     * 做了如下工作,无关顺序
     * 1,从ContextImpl获取LoadedApk的对象mPackageInfo
     * 2,分别替换mPackageInfo的:{mClassLoader, mResDir, mResources, mApplication}
     * 3,mPackageInfo.getResources(mActivityThread)
     * 4,初始化realApplication
     * 5,hostApplication.mOuterContext = realApplication
     * 6,mPackageInfo.mActivityThread.mInitialApplication = realApplication
     * 7,mActivityThread.mAllApplications 中,如果有hostApplication,则hostApplication=realApplication
     * 8,realApplication.attach(hostApplication)
     * 9,realApplication.onCreate()
     */
    private void replaceFileds() throws IllegalAccessException, NoSuchMethodException,
            InvocationTargetException, ClassNotFoundException, InstantiationException {

        // 反射获取 PackageInfo 成员, 注意:这是一个LoadedApk类对象,并非PackageInfo对象
        Object mPackageInfo = ReflectionUtils.getFieldObject(mContxt, "mPackageInfo");

        // 替换 mClassLoader
        ReflectionUtils.setField(mPackageInfo, "mClassLoader", mClassLoader);

        // 被加载apk的绝对路径
        String resPath = new File(mContxt.getDir(DEXPATH, Context.MODE_PRIVATE), mApkName)
                .getAbsolutePath();

        // mResDir -> .apk路径
        ReflectionUtils.setField(mPackageInfo, "mResDir", resPath);
        // mResources -> null
        ReflectionUtils.setField(mPackageInfo, "mResources", null);

        // mPackageInfo.getResources(mActivityThread)
        Object mActivityThread = ReflectionUtils.getFieldObject(mPackageInfo, "mActivityThread");
        Method getResources = mPackageInfo.getClass().getDeclaredMethod("getResources",
                mActivityThread.getClass());
        /*Object res = */
        getResources.invoke(mPackageInfo, mActivityThread); //replace mResoures
//        ReflectionUtils.setField(mApplication.getBaseContext(), "mResources", res);

        // real application
        Application realApplication = (Application) Class.forName(mApplicationName, true, mClassLoader)
                .newInstance(); //here, mContext.getClassLoader == mClassLoader
        // mApplication -> realApplication
        ReflectionUtils.setField(mPackageInfo, "mApplication", realApplication);
        ReflectionUtils.setField(mApplication.getBaseContext(), "mOuterContext", realApplication);
        ReflectionUtils.setField(mActivityThread, "mInitialApplication", realApplication);

        ArrayList<Application> appList = (ArrayList<Application>) ReflectionUtils.getFieldObject
                (mActivityThread, "mAllApplications");

        // why?
        for (Application app : appList) {
            Log.d("result", app.toString());
            if (app == mApplication)    // host application?
                app = realApplication;
        }
        // realApplication.attach(hostApplication)
        Method attachMethod = Application.class.getDeclaredMethod("attach", Context.class);
        attachMethod.setAccessible(true);
        attachMethod.invoke(realApplication, mApplication.getBaseContext());

        realApplication.onCreate();
    }

    private void initDexFile() throws IOException {
        copyFile();
    }

    private void initClassLoader() {
        String dexPaths = null;
        File dexPath = mContxt.getDir(DEXPATH, Context.MODE_PRIVATE);
        File dexApk = new File(dexPath, mApkName);

        if (TextUtils.isEmpty(dexPaths)) {
            dexPaths = dexApk.getAbsolutePath();
        } else {
            dexPaths += File.pathSeparator + dexApk.getAbsolutePath();
        }

        Log.d(TAG, "dexPaths: " + dexPaths);

        File odexPath = mContxt.getDir(ODEX_PATH, Context.MODE_PRIVATE);
        mClassLoader = new DexClassLoader(dexPaths, odexPath.getAbsolutePath(), null, mContxt
                .getClassLoader());
        Log.d(TAG, "mClassLoader: " + mClassLoader);

    }

    /*
     *  copy asset file to dex path
     */
    private void copyFile() throws IOException {
        InputStream input = mContxt.getAssets().open(mApkName);
        File dexPath = mContxt.getDir(DEXPATH, Context.MODE_PRIVATE);
        if (!dexPath.exists()) {
            dexPath.mkdirs();
        }
        File outFile = new File(dexPath, mApkName);
        FileOutputStream out = new FileOutputStream(outFile);
        byte[] bytes = new byte[1024 * 5];
        int len;
        while ((len = input.read(bytes)) != -1) {
            out.write(bytes, 0, len);
        }
        out.close();
        input.close();
        Log.d(TAG, "FILE COPY FINISHED : " + outFile.getAbsolutePath());
    }

}
