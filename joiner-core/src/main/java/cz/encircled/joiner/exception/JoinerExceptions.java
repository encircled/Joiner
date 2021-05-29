package cz.encircled.joiner.exception;

/**
 * Basic class for all Joiner runtime exceptions
 *
 * @author Kisel on 25.01.2016.
 */
public class JoinerExceptions {

    public static JoinerException entityNotFound() {
        return new JoinerException("FindOne returned no result, exactly 1 is expected");
    }

    public static JoinerException multipleEntitiesFound() {
        return new JoinerException("FindOne returned multiple result");
    }

}
