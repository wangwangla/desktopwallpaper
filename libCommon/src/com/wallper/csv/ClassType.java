package com.wallper.csv;

import com.badlogic.gdx.utils.Array;

import java.lang.reflect.Field;

public class ClassType {
    static Array<Class<?>> arry = new Array<>();
    static {
        arry.add(byte.class);
        arry.add(int.class);
        arry.add(float.class);
        arry.add(double.class);
        arry.add(long.class);
        arry.add(boolean.class);
    }

    public static Class<?> containsType(Class<?> type){
        if (arry.contains(type,true)) {
            return type;
        }
        return String.class;
    }

    public static boolean feildBoolean(Field field, String type){
        return field.getGenericType().toString().equals(type);
    }

}
