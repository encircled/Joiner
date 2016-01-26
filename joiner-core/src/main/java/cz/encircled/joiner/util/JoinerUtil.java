package cz.encircled.joiner.util;

import java.lang.reflect.Field;

import com.mysema.codegen.StringUtils;
import com.mysema.query.types.CollectionExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;
import com.mysema.query.types.path.SetPath;
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
        String simpleName = targetClass.getSimpleName();
        String instanceName = StringUtils.uncapitalize(simpleName.substring(1, simpleName.length()));

        Field instanceField = ReflectionUtils.findField(targetClass, instanceName);
        return (Path) ReflectionUtils.getField(instanceField, null);
    }

    @SuppressWarnings("unchecked")
    public static Path<Object> getDefaultAlias(EntityPath<?> path) {
        Class<?> targetClass = path.getClass();
        String simpleName = targetClass.getSimpleName();
        String instanceName = StringUtils.uncapitalize(simpleName.substring(1, simpleName.length()));

        Field instanceField = ReflectionUtils.findField(targetClass, instanceName);
        return (Path) ReflectionUtils.getField(instanceField, null);
    }

}
