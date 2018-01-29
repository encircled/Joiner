package cz.encircled.joiner.util;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.exception.JoinerException;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vlad on 14-Aug-16.
 */
public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<?> generatedClass, Object... arguments) {
        Assert.notNull(generatedClass);

        try {
            // TODO nulls?
            Class[] classesOfArgs = new Class[arguments.length];
            for (int i = 0; i < arguments.length; i++) {
                classesOfArgs[i] = arguments[i].getClass();
            }

            Constructor<?> constructor = findConstructor(generatedClass, classesOfArgs);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return (T) constructor.newInstance(arguments);
        } catch (Exception e) {
            throw new JoinerException("Failed to create new instance of " + generatedClass, e);
        }
    }

    public static Constructor<?> findConstructor(Class<?> clazz, Class[] argumentClasses) {
        outer:
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            final Class<?>[] params = constructor.getParameterTypes();
            if (params.length == argumentClasses.length) {
                for (int i = 0; i < argumentClasses.length; i++) {
                    if (!params[i].isAssignableFrom(argumentClasses[i])) {
                        continue outer;
                    }
                }
                return constructor;
            }
        }

        throw new RuntimeException("Constructor with params [" + Arrays.toString(argumentClasses) + "] is not found in [" + clazz + "]");
    }

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

    public static Set<Class> getSubclasses(Class<?> parent, EntityManager entityManager) {
        return entityManager.getMetamodel().getEntities().stream()
                .filter(entityType -> parent != entityType.getJavaType() && parent.isAssignableFrom(entityType.getJavaType()))
                .map(Type::getJavaType)
                .collect(Collectors.toCollection(HashSet::new));
    }

}
