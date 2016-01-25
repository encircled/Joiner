package cz.encircled.joiner;

import java.lang.reflect.Field;

import com.mysema.codegen.StringUtils;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 25.01.2016.
 */
public class JoinerUtil {

    private static Field listQueryTypeField;

    static {
        listQueryTypeField = ReflectionUtils.findField(ListPath.class, "queryType");
    }

    @SuppressWarnings("unchecked")
    public static Path<Object> getDefaultAlias(ListPath<?, ?> listPath) {
        ReflectionUtils.makeAccessible(listQueryTypeField);
        Class<?> targetClass = (Class<?>) ReflectionUtils.getField(listQueryTypeField, listPath);
        String simpleName = targetClass.getSimpleName();
        String instanceName = StringUtils.uncapitalize(simpleName.substring(1, simpleName.length()));

        Field instanceField = ReflectionUtils.findField(targetClass, instanceName);
        return (Path) ReflectionUtils.getField(instanceField, null);
    }

}
