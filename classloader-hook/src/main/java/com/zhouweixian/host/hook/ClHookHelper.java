package com.zhouweixian.host.hook;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.zhouweixian.host.App;
import com.zhouweixian.host.MainActivity;
import com.zhouweixian.host.util.ReflectUtil;
import com.zhouweixian.host.util.Utils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * Created by leochou on 2017/6/16 13:33
 */

public class ClHookHelper {

    private static final String AT_CLS = "android.app.ActivityThread";
    private static final String GPI_MTD = "getPackageInfoNoCheck";
    private static final String PP_CLS = "android.content.pm.PackageParser";
    private static final String PPP_CLS = "android.content.pm.PackageParser$Package";
    private static final String PUS_CLS = "android.content.pm.PackageUserState";
    private static final String GAI_MTD = "generateApplicationInfo";
    public static void addPackage(File apkFile) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, InstantiationException {

        // 获取ActivityThread
        Class<?> atClass = Class.forName(AT_CLS);
        Method currentActivityThreadMethod = atClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        // 获取ActivityThread 对象
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 获取mPackages 对象
        Field mPackagesField = atClass.getDeclaredField("mPackages");
        mPackagesField.setAccessible(true);

        // final ArrayMap<String, WeakReference<LoadedApk>> mPackages
        Map mPackages = (Map) mPackagesField.get(currentActivityThread);

        // 下面,造一个,加进去
        /* ActivityThread 部分源码
         * case LAUNCH_ACTIVITY: {
                 Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
                 final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

                 r.packageInfo = getPackageInfoNoCheck(
                 r.activityInfo.applicationInfo, r.compatInfo);
                 handleLaunchActivity(r, null);
                 Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
         */
        // 从getPackageInfoNoCheck 入手,需要两个参数
        // 1 ApplicationInfo
        // 我们用系统的PackageParser创建ApplicationInfo
        Class<?> ppClass = Class.forName(PP_CLS);
        Class<?> pppClass = Class.forName(PPP_CLS);
        Class<?> pusClass = Class.forName(PUS_CLS);
        Method generationApplicationInfoMethod = ppClass.getDeclaredMethod(GAI_MTD, pppClass, int.class, pusClass);
        generationApplicationInfoMethod.setAccessible(true);

        // 调用这个方法,有需要三个参数
        // public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state)

        // 1 Package
        Method parsePackageMethod = ppClass.getDeclaredMethod("parsePackage", File.class, int.class);
        parsePackageMethod.setAccessible(true);
        Object pkg = parsePackageMethod.invoke(ppClass.newInstance(), apkFile, 0);

        // 2 flag 0
        // 3 PackageUserState, 直接创建一个

        // ok 合成
        ApplicationInfo applicationInfo = (ApplicationInfo) generationApplicationInfoMethod.invoke(null, pkg, 0, pusClass.newInstance());
        applicationInfo.sourceDir = apkFile.getPath();
        applicationInfo.publicSourceDir = apkFile.getPath();

        // 2 CompatibilityInfo 默认


        // 开始总装

        Class<?> ciClass = Class.forName(CI_CLS);
        Field default_compatibility_info = ciClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        default_compatibility_info.setAccessible(true);
        Object defaultCompatibilityInfo = default_compatibility_info.get(null);
        Method getPackageInfoNoCheckMethod = atClass.getDeclaredMethod(GPI_MTD, ApplicationInfo.class, ciClass);
        Object loadedApk = getPackageInfoNoCheckMethod.invoke(currentActivityThread, applicationInfo, defaultCompatibilityInfo);

        Field mClassLoaderField = loadedApk.getClass().getDeclaredField("mClassLoader");

        // 替换classloader
        mClassLoaderField.setAccessible(true);
        mClassLoaderField.set(loadedApk, new DexClassLoader(apkFile.getPath(),
                Utils.getPluginOptDexDir(applicationInfo.packageName).getPath(),
                Utils.getPluginLibDir(applicationInfo.packageName).getPath(),
                ClassLoader.getSystemClassLoader()));

        Object thisLoadedApk = ((WeakReference) mPackages.get(App.instance.getPackageName())).get();
        // 添加目录 mDataDir
        Field mDataDirField = loadedApk.getClass().getDeclaredField("mDataDir");
        mDataDirField.setAccessible(true);
        mDataDirField.set(loadedApk, ReflectUtil.getFieldObject(thisLoadedApk, "mDataDir"));

        // 设置 mDataDirFile
        Field mDataDirFileField = loadedApk.getClass().getDeclaredField("mDataDirFile");
        mDataDirFileField.setAccessible(true);
        mDataDirFileField.set(loadedApk, ReflectUtil.getFieldObject(thisLoadedApk, "mDataDirFile"));

        // 设置 mLibDir
        Field mLibDirField = loadedApk.getClass().getDeclaredField("mLibDir");
        mLibDirField.setAccessible(true);
        mLibDirField.set(loadedApk, ReflectUtil.getFieldObject(thisLoadedApk, "mLibDir"));

        WeakReference wrf = new WeakReference(loadedApk);
        mPackages.put(applicationInfo.packageName, wrf);

    }

    private static final String CI_CLS = "android.content.res.CompatibilityInfo";

}
