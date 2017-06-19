package com.zhouweixian.host.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Created by leochou on 2017/6/16 20:40
 */

public class ReflectUtil {

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


    public static Field getField(Object classObject, String filedName) throws
            IllegalAccessException {
        Field field = null;
        Class<?> tClass = classObject.getClass();
        while (field == null) {
            try {
                field = tClass.getDeclaredField(filedName);
                field.setAccessible(true);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                if (tClass.getSuperclass() != null) {
                    tClass = tClass.getSuperclass();
                } else {
                    break;
                }
            }
        }

        return field;
    }

    public static Object getFieldObject(Object classObject, String filedName) throws
            IllegalAccessException {
        Field field = getField(classObject, filedName);
        return field.get(classObject);
    }


    public static void setField(Object obj, String fieldName, Object value) throws
            IllegalAccessException {
        Field filed = getField(obj, fieldName);
        filed.set(obj, value);
    }
}
