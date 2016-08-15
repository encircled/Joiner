package cz.encircled.joiner.util;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.exception.JoinerException;

import java.lang.reflect.Constructor;

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

}
