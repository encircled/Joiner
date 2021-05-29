package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerExceptions
import java.util.*

/**
 * Represents a single execution step in a chain
 */
interface ExecutionStep<T> {

    fun perform(arg : List<Any>?) : T

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

    override fun perform(arg : List<Any>?) : T = value

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

    override fun perform(arg : List<Any>?) : T {
        return if (isMono) {
            if (arg.isNullOrEmpty()) {
                throw JoinerExceptions.entityNotFound()
            } else if (arg.size > 1) {
                throw JoinerExceptions.multipleEntitiesFound()
            }
            return callback(arg[0])
        } else callback(arg!!)
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

    override fun perform(arg : List<Any>?) : T {
        return when {
                arg.isNullOrEmpty() -> {
                    callback(Optional.empty())
                }
                arg.size > 1 -> {
                    throw JoinerExceptions.multipleEntitiesFound()
                }
                else -> callback(Optional.ofNullable(arg[0]))
            }
    }

}
