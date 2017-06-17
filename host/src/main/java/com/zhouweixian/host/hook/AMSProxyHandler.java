package com.zhouweixian.host.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.zhouweixian.host.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by leochou on 2017/6/16 14:23
 */

class AMSProxyHandler implements InvocationHandler {

    private static final String METHOD = "startActivity";
    private static final String TAG = "AMSProxyHandler";
    private static final String RAW_INTENT_EXTRA = "raw_intent";
    private final Object base;
    private final Context context;

    public AMSProxyHandler(Object rawIAM, Context context) {
        this.base = rawIAM;
        this.context = context;
    }

    public boolean inManifest(Context context, String name) throws PackageManager.NameNotFoundException {
        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        for (int i = 0; i < packageInfo.activities.length; i++) {
            if (packageInfo.activities[i].name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (METHOD.equals(method.getName())) {

            Log.d(TAG, "METHOD: " + METHOD);

            int intentIndex = 0;
            // IActivityManager.startActivity(...), 从参数中查找出Intent，换成我们的，
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    intentIndex = i;
                }
            }

            Intent rawIntent = (Intent) args[intentIndex];

            if (inManifest(context,rawIntent.getComponent().getClassName()))
                return method.invoke(base, args);

            Intent intent = new Intent();
            ComponentName componentName = new ComponentName("com.zhouweixian.host", StubActivity.class.getName());
            intent.setComponent(componentName);
            intent.putExtra(RAW_INTENT_EXTRA, rawIntent);
            args[intentIndex] = intent;
            Log.d(TAG, "----FINISHED AMS HOOK----");
            Log.d(TAG, "----" + intent.toString() + "----");
            return method.invoke(base, args);
        }

        return method.invoke(base, args);
    }
}
