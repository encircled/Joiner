package cz.encircled.joiner.exception;

/**
 * This exception is thrown, when there are multiple join aliases in a query
 *
 * @author Kisel on 26.01.2016.
 */
public class AliasAlreadyUsedException extends JoinerException {

    public AliasAlreadyUsedException(final String message) {
        super(message);
    }

}
