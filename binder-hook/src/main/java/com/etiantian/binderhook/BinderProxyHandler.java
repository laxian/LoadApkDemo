package com.etiantian.binderhook;

import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by zwx on 17-6-15
 */

public class BinderProxyHandler implements InvocationHandler {

    private static final String STUBCLASS = "android.content.IClipboard$Stub";
    private static final String ASINTERFACE = "asInterface";
    private static final String TAG = "BinderProxyHandler";
    private final String METHOD_NAME = "queryLocalInterface";
    IBinder base;
    IInterface asInterface;

    public BinderProxyHandler(IBinder binder) {
        this.base = binder;
        try {
            // 获取 IInterFace.Stub, 调用asInterface
            Class<?> stubClass = Class.forName(STUBCLASS);
            Method asInterfaceMethod = stubClass.getDeclaredMethod(ASINTERFACE, IBinder.class);
            asInterfaceMethod.setAccessible(true);
            asInterface = (IInterface) asInterfaceMethod.invoke(null, base);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 如果调用了IBinder 的 queryLocalInterface 方法,用我们的代理处理
        Log.d(TAG, method.getName());
        if (method.getName().equals(METHOD_NAME)) {

            Log.d(TAG, "------------>");
            return Proxy.newProxyInstance(asInterface.getClass().getClassLoader(), asInterface.getClass().getInterfaces(),
                    new IInterfaceProxyHandler());
        } else {
            Log.d(TAG, "<------------");

        }

        return method.invoke(proxy, args);
    }
}