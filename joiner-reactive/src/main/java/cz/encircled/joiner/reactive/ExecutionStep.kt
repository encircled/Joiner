package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerExceptions
import java.util.*

/**
 * Represents a single execution step in a chain
 */
interface ExecutionStep<T> {

    fun perform(arg: List<Any>?): T

    fun <T> extractExactlyOne(arg: List<Any>?): T {
        return when {
            arg.isNullOrEmpty() -> {
                throw JoinerExceptions.entityNotFound()
            }
            arg.size > 1 -> {
                throw JoinerExceptions.multipleEntitiesFound()
            }
            else -> arg[0] as T
        }
    }

    fun <T> extractAtMostOne(arg: List<Any>?): T? {
        return when {
            arg.isNullOrEmpty() -> null
            arg.size > 1 -> {
                throw JoinerExceptions.multipleEntitiesFound()
            }
            else -> arg[0] as T
        }
    }

}

/**
 * Execution step which returns static [value]
 */
class SyncExecutionStep<T>(
    /**
     * Passed by the user
     */
    private val value: T
) : ExecutionStep<T> {

    override fun perform(arg: List<Any>?): T = value

}

/**
 * Represents an async function, passed by the user
 */
class AsyncExecutionStep<T>(
    /**
     * Defines whether target step expects a singular or plural input parameter
     */
    private val isMono: Boolean,

    /**
     * Function to be executed
     */
    private val callback: (Any) -> T
) : ExecutionStep<T> {

    override fun perform(arg: List<Any>?): T {
        return if (isMono) callback(extractExactlyOne(arg))
        else callback(arg!!)
    }

}

/**
 * Represents an async function, passed by the user
 */
class OptionalAsyncExecutionStep<T>(
    /**
     * Function to be executed
     */
    private val callback: (Optional<Any>) -> T
) : ExecutionStep<T> {

    override fun perform(arg: List<Any>?): T =
        callback(Optional.ofNullable(extractAtMostOne(arg)))

}
