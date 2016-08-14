package cz.encircled.joiner.util;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.exception.JoinerException;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author Kisel on 25.01.2016.
 */
public class JoinerUtil {

    @SuppressWarnings("unchecked")
    public static <T extends EntityPath> T instantiate(Class<? extends EntityPath> generatedClass, String alias) {
        Assert.notNull(alias);

        try {
            Constructor<? extends EntityPath> constructor = generatedClass.getConstructor(String.class);
            return (T) constructor.newInstance(alias);
        } catch (NoSuchMethodException e) {
            throw new JoinerException("EntityPath String constructor is missing on " + generatedClass);
        } catch (Exception e) {
            throw new JoinerException("Failed to create new instance of " + generatedClass, e);
        }
    }

    public static <T extends EntityPath> T getAliasForChild(EntityPath<?> parent, T childPath) {
        return JoinerUtil.instantiate(childPath.getClass(), childPath.toString() + "_on_" + parent.toString());
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
