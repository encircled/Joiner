package cz.encircled.joiner.exception;

/**
 * This exception is thrown, when a predicate or a join have a reference to an alias, that is not present in a query
 *
 * @author Kisel on 26.01.2016.
 */
public class AliasMissingException extends JoinerException {

    public AliasMissingException(String message) {
        super(message);
    }

}
