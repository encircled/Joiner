package cz.encircled.joiner.exception

/**
 * Basic class for all Joiner runtime exceptions

 * @author Kisel on 25.01.2016.
 */
open class JoinerException : RuntimeException {

    constructor(message: String) : super(message) {
    }

    constructor(message: String, exception: Exception) : super(message, exception) {
    }
}
