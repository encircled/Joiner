package cz.encircled.joiner.util;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.exception.JoinerException;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vlad on 14-Aug-16.
 */
public class ReflectionUtils {

    @SuppressWarnings("unchecked")
    public static <T extends EntityPath> T instantiate(Class<? extends EntityPath> generatedClass, String alias) {
        Assert.notNull(generatedClass);

        try {
            Constructor<? extends EntityPath> constructor = generatedClass.getConstructor(String.class);
            return (T) constructor.newInstance(alias);
        } catch (Exception e) {
            throw new JoinerException("Failed to create new instance of " + generatedClass, e);
        }
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
