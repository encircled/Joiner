package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.getAtMostOne
import cz.encircled.joiner.reactive.ReactorExtension.getExactlyOne
import java.util.*

/**
 * A single execution step in a chain, represented as a pre-computed value or a callback provided by user.
 *
 * Result of the [ExecutionStep] will be then asynchronously processed by Joiner as follows:
 * - return as is, if the result is a [java.util.concurrent.CompletableFuture], for cases like: map, filter etc
 * - run the DB query, if the result is a [cz.encircled.joiner.query.JoinerQuery]
 * - otherwise, do persist an entity (or list of entities) and return its reference
 */
interface ExecutionStep<T> {

    /**
     * Execute a user callback or return pre-computed value
     */
    fun perform(arg: Any): T

    /**
     * Convert step result if needed (e.g. wrap as [Optional] in case of nullable steps, or extract a singular value in case of *findOne*)
     *
     * @param arg initial step result, always passed as a [List], even in case of functions like *findOne*
     */
    fun convertResult(arg: List<Any>): Any = arg

}

/**
 * Execution step which returns pre-computed [value] with plural result (e.g. *FindMultiple* query)
 */
open class SyncExecutionStep<T>(private val value: T) : ExecutionStep<T> {
    override fun perform(arg: Any): T = value
}

/**
 * Execution step which returns pre-computed [value]  with singular result (e.g. *FindOne* query)
 */
class MonoSyncExecutionStep<T>(value: T) : SyncExecutionStep<T>(value) {
    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()
}

/**
 * Execution step which returns pre-computed [value] with optional singular result (e.g. *FindOneOptional* query)
 */
class OptionalSyncExecutionStep<T>(value: T) : SyncExecutionStep<T>(value) {
    override fun convertResult(arg: List<Any>): Any = Optional.ofNullable(arg.getAtMostOne())
}

/**
 * Execution step which asynchronously calls a user [callback] with plural result (e.g. *FindMultiple* query)
 */
open class AsyncExecutionStep<F, T>(private val callback: (F) -> T) : ExecutionStep<T> {
    override fun perform(arg: Any): T = callback(arg as F)
}
/**
 * Execution step which asynchronously calls a user [callback] with singular result (e.g. *FindOne* query)
 */
class MonoAsyncExecutionStep<F, T>(callback: (F) -> T) : AsyncExecutionStep<F, T>(callback) {
    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()
}

/**
 * Execution step which asynchronously calls a user [callback] with optional singular result (e.g. *FindOneOptional* query)
 */
class OptionalAsyncExecutionStep<F, T>(callback: (F) -> T) : AsyncExecutionStep<F, T>(callback) {
    override fun convertResult(arg: List<Any>): Any = Optional.ofNullable(arg.getAtMostOne())
}
