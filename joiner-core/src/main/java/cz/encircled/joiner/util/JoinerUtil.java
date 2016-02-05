package cz.encircled.joiner.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mysema.query.types.CollectionExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.SetPath;
import cz.encircled.joiner.exception.JoinerException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 25.01.2016.
 */
public class JoinerUtil {

    private static Field listQueryTypeField;
    private static Field setQueryTypeField;
    private static Map<Class<EntityPath<?>>, EntityPath<?>> pathToDefaultAlias = new ConcurrentHashMap<>();

    static {
        listQueryTypeField = ReflectionUtils.findField(ListPath.class, "queryType");
        setQueryTypeField = ReflectionUtils.findField(SetPath.class, "queryType");
        ReflectionUtils.makeAccessible(listQueryTypeField);
        ReflectionUtils.makeAccessible(setQueryTypeField);
    }

    @SuppressWarnings("unchecked")
    public static EntityPath<?> getDefaultAlias(CollectionExpression<?, ?> path) {
        Class<?> targetClass;
        if (path instanceof ListPath) {
            targetClass = (Class<?>) ReflectionUtils.getField(listQueryTypeField, path);
        } else {
            targetClass = (Class<?>) ReflectionUtils.getField(setQueryTypeField, path);
        }

        Field instanceField = getQInstanceField(targetClass, getSimpleNameOfQ(targetClass));
        return (EntityPath) ReflectionUtils.getField(instanceField, null);
    }

    @SuppressWarnings("unchecked")
    public static EntityPath<?> getDefaultAlias(EntityPath<?> path) {
        return getDefaultAlias((Class<EntityPath<?>>) path.getClass());
    }

    @SuppressWarnings("unchecked")
    public static EntityPath<?> getDefaultAlias(Class<EntityPath<?>> clazz) {
        EntityPath<?> alias = pathToDefaultAlias.get(clazz);
        if (alias == null) {
            Field instanceField = getQInstanceField(clazz, getSimpleNameOfQ(clazz));
            alias = (EntityPath) ReflectionUtils.getField(instanceField, null);
        }
        return alias;
    }

    public static EntityPath<?> getSuper(EntityPath<?> path) {
        Field field = ReflectionUtils.findField(path.getClass(), "_super");
        return field != null ? (EntityPath<?>) ReflectionUtils.getField(field, path) : null;
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityPath<T> getGenerated(Path<T> path, String alias) {
        Class<? extends T> clazz = path.getType();
        String generatedName = clazz.getPackage().getName() + ".Q" + clazz.getSimpleName();
        Class<EntityPath<?>> generatedClass;
        try {
            Class<?> candidate = Class.forName(generatedName);
            if (!EntityPath.class.isAssignableFrom(candidate)) {
                throw new JoinerException("QueryDSL generated class expected for name " + generatedName);
            }
            generatedClass = (Class<EntityPath<?>>) candidate;
        } catch (ClassNotFoundException e) {
            throw new JoinerException("QueryDSL generated class not found for name " + generatedName);
        }

        return instantiate(generatedClass, alias);
    }

    @SuppressWarnings("unchecked")
    public static <T> EntityPath<T> instantiate(Class<EntityPath<?>> generatedClass, String alias) {
        if (alias == null) {
            alias = getDefaultAlias(generatedClass).toString();
        }

        try {
            Constructor<EntityPath<?>> constructor = generatedClass.getConstructor(String.class);
            return (EntityPath<T>) constructor.newInstance(alias);
        } catch (NoSuchMethodException e) {
            throw new JoinerException("EntityPath String constructor is missing on " + generatedClass);
        } catch (Exception e) {
            throw new JoinerException("Failed to create new instance of " + generatedClass, e);
        }
    }

    public static Object findAndGetField(Object object, String name) {
        Field field = ReflectionUtils.findField(object.getClass(), name);
        return ReflectionUtils.getField(field, object);
    }

    private static Field getQInstanceField(Class<?> targetClass, String instanceName) {
        Field instanceField = ReflectionUtils.findField(targetClass, instanceName);
        // If a field has the same name as it's class - a number (starting from 1) is appended to Q instance field and alias so
        for (int i = 1; i < 50; i++) {
            Field candidate = ReflectionUtils.findField(targetClass, instanceName + i);
            if (candidate != null) {
                instanceField = candidate;
            } else {
                break;
            }
        }
        return instanceField;
    }

    private static String getSimpleNameOfQ(Class<?> targetClass) {
        String simpleName = targetClass.getSimpleName();
        return StringUtils.uncapitalize(simpleName.substring(1, simpleName.length()));
    }

}
