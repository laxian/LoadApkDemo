package com.etiantian.binderhook;

import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by zwx on 17-6-15
 */

public class BinderHookHelper {

    private static final String TAG = "BinderHookHelper";
    private static final String SERVICE_MANAGER = "android.os.ServiceManager";
    private static final String SERVICE_NAME = "clipboard";
    private static final String FIELD = "sCache";

    public static void hook() {
        try {
            // 获取ServiceManager 的 Class
            Class<?> smClass = Class.forName(SERVICE_MANAGER);
            // 获取ServiceManager 的 getService 方法
            Method getServiceMethod = smClass.getDeclaredMethod("getService", String.class);
            getServiceMethod.setAccessible(true);
            // 通过getService(serviceName) 获取服务的IBinder 对象 rawBinder
            IBinder rawBinder = (IBinder) getServiceMethod.invoke(null, SERVICE_NAME);
            // 生成 rawBinder 的动态代理
            IBinder proxyInstance = (IBinder) Proxy.newProxyInstance(rawBinder.getClass().getClassLoader(),
                    rawBinder.getClass().getInterfaces(), new BinderProxyHandler(rawBinder));

            // 将动态代理加入到 ServiceManager 的 sCache 中,达到替换的目的
            Field sCacheField = smClass.getDeclaredField(FIELD);
            sCacheField.setAccessible(true);
            Map<String, IBinder> sCache = (Map<String, IBinder>) sCacheField.get(null);
            sCache.put(SERVICE_NAME, proxyInstance);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }


}
