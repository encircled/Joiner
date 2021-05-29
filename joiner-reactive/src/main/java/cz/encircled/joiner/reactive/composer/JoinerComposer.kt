package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

/**
 * Composes multiple async functions into a single execution chain to run it in a single DB transaction
 *
 * @param ENTITY class of result entity
 * @param ENTITY_CONTAINER class of entity container (i.e. List) in case of plural result, otherwise same as [ENTITY]
 * @param PUBLISHER type of result publisher: [Mono] for singular result, [Flux] for plural
 */
open class JoinerComposer<ENTITY, ENTITY_CONTAINER, PUBLISHER>(
    /**
     * Defines whether final result set is singular or plural
     */
    private val isMono: Boolean,

    /**
     * List of async steps to be executed
     */
    val steps: MutableList<ExecutionStep<*>>
) {

    private var isChainStarted = false

    internal open fun execute(r: ReactorJoiner): PUBLISHER = throw IllegalStateException("Composer is empty")

    fun <F, R> findOne(query: JoinerQuery<F, R>): MonoJoinerComposer<R> {
        startChain()
        return singular(query)
    }

    fun <F, R> findOneOptional(query: JoinerQuery<F, R>): OptionalMonoJoinerComposer<R> {
        startChain()
        return optional(query)
    }

    fun <F, R> find(query: JoinerQuery<F, R>): FluxJoinerComposer<R> {
        startChain()
        return plural(query)
    }

    fun <E : Any> persist(entity: E): MonoJoinerComposer<E> {
        startChain()
        return singular(entity)
    }

    fun <E : Any> persist(entity: List<E>): FluxJoinerComposer<E>  {
        startChain()
        return plural(entity)
    }

    /**
     * Creates new composer with singular (Mono) result projection
     */
    protected fun <R> singular(callback: (ENTITY_CONTAINER) -> Any): MonoJoinerComposer<R> {
        steps.add(AsyncExecutionStep(isMono, callback as (Any?) -> Any))
        return MonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with singular (Mono) result projection
     */
    protected fun <R> singular(value: Any): MonoJoinerComposer<R> {
        steps.add(SyncExecutionStep(value))
        return MonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with optional singular (Mono) result projection
     */
    protected fun <R> optional(value: Any): OptionalMonoJoinerComposer<R> {
        steps.add(SyncExecutionStep(value))
        return OptionalMonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with optional singular (Mono) result projection
     */
    protected fun <R> optional(callback: (Optional<ENTITY_CONTAINER>) -> Any): OptionalMonoJoinerComposer<R> {
        steps.add(OptionalAsyncExecutionStep(callback as (Any?) -> Any))
        return OptionalMonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with plural (Flux) result projection
     */
    protected fun <R> plural(callback: (ENTITY_CONTAINER) -> Any): FluxJoinerComposer<R> {
        steps.add(AsyncExecutionStep(isMono, callback as (Any?) -> Any))
        return FluxJoinerComposer(steps)
    }

    /**
     * Creates new composer with plural (Flux) result projection
     */
    protected fun <R> plural(value: Any): FluxJoinerComposer<R> {
        steps.add(SyncExecutionStep(value))
        return FluxJoinerComposer(steps)
    }

    private fun startChain() {
        if (isChainStarted) throw IllegalStateException("Multiple execution chains per single transaction are not supported!")
        else isChainStarted = true
    }

}