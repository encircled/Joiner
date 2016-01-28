package cz.encircled.joiner.util;

import java.lang.reflect.Field;

import com.mysema.query.types.CollectionExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.SetPath;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 25.01.2016.
 */
public class JoinerUtil {

    private static Field listQueryTypeField;
    private static Field setQueryTypeField;

    static {
        listQueryTypeField = ReflectionUtils.findField(ListPath.class, "queryType");
        setQueryTypeField = ReflectionUtils.findField(SetPath.class, "queryType");
        ReflectionUtils.makeAccessible(listQueryTypeField);
        ReflectionUtils.makeAccessible(setQueryTypeField);
    }

    @SuppressWarnings("unchecked")
    public static Path<Object> getDefaultAlias(CollectionExpression<?, ?> path) {
        Class<?> targetClass;
        if (path instanceof ListPath) {
            targetClass = (Class<?>) ReflectionUtils.getField(listQueryTypeField, path);
        } else {
            targetClass = (Class<?>) ReflectionUtils.getField(setQueryTypeField, path);
        }

        Field instanceField = getQInstanceField(targetClass, getSimpleNameOfQ(targetClass));
        return (Path) ReflectionUtils.getField(instanceField, null);
    }


    // TODO add caching
    @SuppressWarnings("unchecked")
    public static Path<Object> getDefaultAlias(EntityPath<?> path) {
        Class<?> targetClass = path.getClass();
        Field instanceField = getQInstanceField(targetClass, getSimpleNameOfQ(targetClass));

        return (Path) ReflectionUtils.getField(instanceField, null);
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
