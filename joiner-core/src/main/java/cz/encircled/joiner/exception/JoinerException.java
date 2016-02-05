package cz.encircled.joiner.exception;

/**
 * Basic class for all Joiner runtime exceptions
 *
 * @author Kisel on 25.01.2016.
 */
public class JoinerException extends RuntimeException {

    public JoinerException(String message) {
        super(message);
    }

    public JoinerException(String message, Exception exception) {
        super(message, exception);
    }
}
