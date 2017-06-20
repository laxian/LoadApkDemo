package com.zhouweixian.host.hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by leochou on 2017/6/16 15:01
 */

class CustomActivityCallback implements Handler.Callback {

    private static final String RAW_INTENT_EXTRA = "raw_intent";
    private final Handler base;

    public CustomActivityCallback(Handler mH) {
        this.base = mH;
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == 100) {
            // LAUNCH_ACTIVITY         = 100;
             Object acr = msg.obj;
            try {
                Field intentField = acr.getClass().getDeclaredField("intent");
                intentField.setAccessible(true);
                Intent intent = (Intent) intentField.get(acr);
                Intent realIntent = null;
                if (intent.getExtras() != null) {
                    realIntent = intent.getParcelableExtra(RAW_INTENT_EXTRA);
                    intent.setComponent(realIntent.getComponent());
                }
                Field activityInfoField = acr.getClass().getDeclaredField("activityInfo");
                activityInfoField.setAccessible(true);
                ActivityInfo ai = (ActivityInfo) activityInfoField.get(acr);
                ai.applicationInfo.packageName = realIntent.getPackage() == null ? realIntent.getComponent().getPackageName() : realIntent.getPackage();

//                hookPackageManager();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        base.handleMessage(msg);
        return true;
    }


    private static void hookPackageManager() throws Exception {

        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.

        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        // 获取ActivityThread里面原始的 sPackageManager
        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[] { iPackageManagerInterface },
                new IPackageManagerHookHandler(sPackageManager));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        sPackageManagerField.set(currentActivityThread, proxy);
    }
}
