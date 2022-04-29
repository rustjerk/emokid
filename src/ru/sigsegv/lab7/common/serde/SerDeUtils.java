package ru.sigsegv.lab7.common.serde;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Various (de)serialization utilities
 */
public class SerDeUtils {
    /**
     * Enquotes a string, escaping various characters
     *
     * @param str string to enquote
     * @return quoted string
     */
    public static String enquote(String str) {
        return "\"" + str
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace("\f", "\\f")
                .replace("\b", "\\b")
                .replace("\"", "\\\"") + "\"";
    }

    /**
     * Finds a public constructor without arguments
     *
     * @param clazz class to inspect
     * @param <T>   type of the class
     * @return public constructor without arguments, or null if it doesn't exist
     */
    public static <T> Constructor<T> getPublicCtor(Class<T> clazz) {
        try {
            Constructor<T> ctor = clazz.getConstructor();
            if (!Modifier.isPublic(ctor.getModifiers()))
                return null;
            return ctor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    /**
     * Finds a public method with given arguments
     *
     * @param clazz class to inspect
     * @param name  name of the method
     * @param args  arguments of the method
     * @return method, or null if it doesn't exist
     */
    public static Method getPublicMethod(Class<?> clazz, String name, Class<?>... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals(Boolean.class)) args[i] = boolean.class;
            if (args[i].equals(Byte.class)) args[i] = byte.class;
            if (args[i].equals(Short.class)) args[i] = short.class;
            if (args[i].equals(Integer.class)) args[i] = int.class;
            if (args[i].equals(Long.class)) args[i] = long.class;
            if (args[i].equals(Float.class)) args[i] = float.class;
            if (args[i].equals(Double.class)) args[i] = double.class;
        }

        try {
            Method method = clazz.getDeclaredMethod(name, args);
            if (!Modifier.isPublic(method.getModifiers()))
                return null;
            return method;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Gets name of field's setter
     *
     * @param fieldName name of the field
     * @return name of the setter
     */
    public static String getSetterName(String fieldName) {
        return "set" + getUpperFieldName(fieldName);
    }

    /**
     * Gets name of field's getter
     *
     * @param fieldName name of the field
     * @return name of the getter
     */
    public static String getGetterName(String fieldName) {
        return "get" + getUpperFieldName(fieldName);
    }

    private static String getUpperFieldName(String fieldName) {
        return fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
    }
}
