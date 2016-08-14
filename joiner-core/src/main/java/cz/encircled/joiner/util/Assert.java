package cz.encircled.joiner.util;

/**
 * @author Vlad on 14-Aug-16.
 */
public class Assert {

    public static void notNull(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Argument must be not null!");
        }
    }

}
