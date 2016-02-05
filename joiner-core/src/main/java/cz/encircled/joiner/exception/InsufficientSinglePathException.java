package cz.encircled.joiner.exception;

/**
 * This exception is thrown, when EntityPath is passed to a single join, for example 'QAddress.address' instead of 'QUser.user.address'
 *
 * @author Kisel on 26.01.2016.
 */
public class InsufficientSinglePathException extends JoinerException {

    public InsufficientSinglePathException(final String message) {
        super(message);
    }

}
