package com.zhouweixian.host.hook;

import android.content.Context;
import android.os.Handler;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Created by leochou on 2017/6/16 13:33
 */

public class AmsHookHelper {

    private static final String AMN = "android.app.ActivityManagerNative";
    private static final String AMN_FIELD = "gDefault";
    private static final String SINGLETON = "android.util.Singleton";
    private static final String IACTIVITYMANAGER = "android.app.IActivityManager";

    public static void hookActivityManagerNative(Context context) {
        try {
            // 找到ActivityManagerNative.gDefault.mInstance, 这是一个IActivityManager对象，将其用代理替换
            // 1 ActivityManagerNative
            Class<?> amnClass = Class.forName(AMN);
            Field gDefaultField = amnClass.getDeclaredField(AMN_FIELD);
            gDefaultField.setAccessible(true);
            // 2 gDefault
            Object gDefault = gDefaultField.get(null);

            Class<?> IAMClass = Class.forName(IACTIVITYMANAGER);

            Class<?> singletonClass = Class.forName(SINGLETON);
            // 3 mInstance
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            Object rawIAM = mInstanceField.get(gDefault);

            // create proxy
            Object proxyInstance = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{IAMClass}, new AMSProxyHandler(rawIAM, context));
            mInstanceField.set(gDefault, proxyInstance);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private static final String AT = "android.app.ActivityThread";
    private static final String ATH = "android.app.ActivityThread$H";
    private static final String ATField = "sCurrentActivityThread";
    public static void hookActivityThreadHandler() {
        try {

            // ActivityThread.H -> Handler
            // 替换掉dispatchMessage方法
            Class<?> atClass = Class.forName(AT);
            Field atField = atClass.getDeclaredField(ATField);
            atField.setAccessible(true);
            Object currentActivityThread = atField.get(null);
            Field mHField = atClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            Handler mH = (Handler) mHField.get(currentActivityThread);

            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);
            mCallbackField.set(mH, new CustomActivityCallback(mH));

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
