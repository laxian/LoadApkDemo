package com.etiantian.binderhook;

import android.content.ClipData;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by zwx on 17-6-15
 */

public class IInterfaceProxyHandler implements InvocationHandler {

    private static final String TAG = "IInterfaceProxyHandler";
    final String HASPRIMARYCLIP = "hasPrimaryClip";
    final String GETPRIMARYCLIP = "getPrimaryClip";

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(HASPRIMARYCLIP)) {
            Log.d(TAG, HASPRIMARYCLIP);
            return true;
        }
        if (method.getName().equals(GETPRIMARYCLIP)) {
            Log.d(TAG, GETPRIMARYCLIP);
            return ClipData.newPlainText("lable", "you are hacked!!!");
        }
        return method.invoke(proxy, args);
    }
}