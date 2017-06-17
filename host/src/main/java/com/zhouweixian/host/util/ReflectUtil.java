package com.zhouweixian.host.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.zhouweixian.host.R;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

/**
 * Created by leochou on 2017/6/16 20:40
 */

public class ReflectUtil {


    @NonNull
    public static DexClassLoader getDexClassLoader(String apkFullPath, String optDir) {
        //参数：
        // 1、包含dex的apk文件或jar文件的路径;
        // 2、apk、jar解压缩生成dex存储的目录;
        // 3、本地library库目录，一般为null;
        // 4、父ClassLoader;
        return new DexClassLoader(
                apkFullPath,
                optDir,
                null,
                ClassLoader.getSystemClassLoader());
    }


    /**
     * 加载apk获得内部资源
     *
     * @param apkPackageName apk文件的包名
     * @param resType        (anim, attr,bool,color,dimen,drawable,id,integer,layout,mipmap,string,style,styleable)
     * @param resName        带查找的资源名
     */
    public static int getResIdFromApk(DexClassLoader dexClassLoader, String apkPackageName, String resType, String resName)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {

        //通过使用apk自己的类加载器，反射出R类中相应的内部类进而获取我们需要的资源id
        Class<?> clazz = dexClassLoader.loadClass(apkPackageName + ".R$" + resType);

        Field field = clazz.getDeclaredField(resName);
        //得到资源id
        return field.getInt(R.id.class);
    }


    /**
     * 加载apk，并返回{className}指定的类
     *
     * @param apkPackageName apk文件的包名
     * @param className      类名
     * @return className对应的类
     */
    public static Class<?> getClassFromApk(DexClassLoader dexClassLoader, String apkPackageName, String className) throws ClassNotFoundException {
        return dexClassLoader.loadClass(apkPackageName + "." + className);
    }

    /**
     * @return 得到对应插件的Resource对象
     */
    public static Resources getPluginResources(String apkFullPath, Resources superRes) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            //反射调用方法addAssetPath(String path)
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            //将未安装的Apk文件的添加进AssetManager中，第二个参数为apk文件的路径带apk名
            addAssetPath.invoke(assetManager, apkFullPath);
            return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void addPathToDexPathList(Context context, DexClassLoader dexClassLoader) {
        try {
            Object elementsPlugin = getElements(dexClassLoader);
            Object elementsBase = getElements(context.getClassLoader());
            Object newElements = combineArray(elementsPlugin, elementsBase);

            Object pathList = getField(BaseDexClassLoader.class, "pathList", context.getClassLoader());
            setField(Class.forName("dalvik.system.DexPathList"),"dexElements", pathList, newElements);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Object getElements(ClassLoader dexClassLoader) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Field pathListField = BaseDexClassLoader.class.getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object pathList = pathListField.get(dexClassLoader);

        Class<?> pathListClass = Class.forName("dalvik.system.DexPathList");
        Field dexElementsField = pathListClass.getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        return dexElementsField.get(pathList);
    }

    /**
     * 通过反射获取对象的属性值
     */
    public static Object getField(Class<?> cl, String fieldName, Object object) throws NoSuchFieldException, IllegalAccessException {
        Field field = cl.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    /**
     * 通过反射设置对象的属性值
     */
    public static void setField(Class<?> cl, String fieldName, Object object, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = cl.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    /**
     * 通过反射合并两个数组
     */
    public static Object combineArray(Object firstArr, Object secondArr) {
        int firstLength = Array.getLength(firstArr);
        int secondLength = Array.getLength(secondArr);
        int length = firstLength + secondLength;

        Class<?> componentType = firstArr.getClass().getComponentType();
        Object newArr = Array.newInstance(componentType, length);
        for (int i = 0; i < length; i++) {
            if (i < firstLength) {
                Array.set(newArr, i, Array.get(firstArr, i));
            } else {
                Array.set(newArr, i, Array.get(secondArr, i - firstLength));
            }
        }
        return newArr;
    }

}
