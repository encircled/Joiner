package cz.encircled.joiner.util;

import java.lang.reflect.Field;

/**
 * @author Vlad on 14-Aug-16.
 */
public class ReflectionUtils {

    public static Field findField(Class<?> clazz, String name) {
        Assert.notNull(clazz);
        Assert.notNull(name);

        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    public static void setField(Field field, Object targetObject, Object value) {
        Assert.notNull(field);

        makeAccessible(field);
        try {
            field.set(targetObject, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getField(Field field, Object object) {
        Assert.notNull(field);
        makeAccessible(field);

        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void makeAccessible(Field field) {
        Assert.notNull(field);

        if (!field.isAccessible()) {
            field.setAccessible(true);
        }
    }

}
