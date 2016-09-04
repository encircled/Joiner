package cz.encircled.joiner.util

/**
 * Assert util methods

 * @author Vlad on 14-Aug-16.
 */
object Assert {

    fun notNull(`object`: Any?) {
        if (`object` == null) {
            throw IllegalArgumentException("Argument must be not null!")
        }
    }

    fun assertThat(predicate: Boolean) {
        if (!predicate) {
            throw IllegalArgumentException("Predicate must be true!")
        }
    }

}
