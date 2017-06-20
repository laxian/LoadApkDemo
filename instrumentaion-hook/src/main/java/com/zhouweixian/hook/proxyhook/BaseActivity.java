package com.zhouweixian.hook.proxyhook;

import android.app.Instrumentation;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BaseActivity extends AppCompatActivity {


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        try {
            // 拿到ActivityThread 类
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            // 拿到ActivityTHread 类的静态方法 currentActivityThreadMethod
            Method currentActivityThreadMethod = activityThreadClass.getMethod("currentActivityThread");
            // 拿到currentActivityThread对象实例
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);
            // 拿到ActivityThread 类的对象属性 mInstrumentation
            Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Object realInstrumentation = mInstrumentationField.get(currentActivityThread);
            // 用这个真实的 realInstrumentation, 伪造一个
            EvilInstrumentation evilInstrumentation = new EvilInstrumentation((Instrumentation) realInstrumentation);
            mInstrumentationField.set(currentActivityThread, evilInstrumentation);
            Log.d("EvilInstrumentation", "finish");

//            Class<?> ActivityThreadClass = Class.forName("android.app.ActivityThread");
//            Field mInstrumentation = ActivityThreadClass.getDeclaredField("mInstrumentation");
//            mInstrumentation.setAccessible(true);
//
//            Method currentActivityThreadMethod = ActivityThreadClass.getMethod("currentActivityThread");
//            Object activityThread = currentActivityThreadMethod.invoke(null);
//
//            Object instrumentation = mInstrumentation.get(activityThread);
//            EvilInstrumentation evilInstrumentation = new EvilInstrumentation((Instrumentation) instrumentation);
//            mInstrumentation.set(activityThread, evilInstrumentation);


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
