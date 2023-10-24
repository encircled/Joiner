package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

/**
 * Composes multiple async functions into a single execution chain to run it in a single DB transaction
 *
 * @param ENTITY class of result entity
 * @param ENTITY_CONTAINER class of entity container (i.e. [List<ENTITY>] in case of plural result), otherwise same as [ENTITY]
 * @param PUBLISHER type of result publisher: [Mono] for singular result, [Flux] for plural
 */
open class JoinerComposer<ENTITY, ENTITY_CONTAINER, PUBLISHER>(
    /**
     * List of async steps to be executed
     */
    val steps: MutableList<ExecutionStep<*>>
) {

    private var isChainStarted = false

    internal open fun executeChain(r: ReactorJoiner): PUBLISHER = throw IllegalStateException("Composer is empty")

    /**
     * Emit specified [items]
     */
    fun <T> just(items: List<T>): FluxJoinerComposer<T> {
        markChainStarted()
        steps.add(ComputedExecutionStep(CompletableFuture.completedFuture(items)))
        return FluxJoinerComposer(steps);
    }

    /**
     * Emit specified [item]
     */
    fun <T> just(item: T): MonoJoinerComposer<T> {
        markChainStarted()
        steps.add(MonoComputedExecutionStep(CompletableFuture.completedFuture(listOf(item))))
        return MonoJoinerComposer(steps);
    }

    /**
     * Execute a select query and expect exactly one result
     */
    fun <F, R> findOne(query: JoinerQuery<F, R>): MonoJoinerComposer<R> {
        markChainStarted()
        return singular(query)
    }

    /**
     * Execute a select query and expect at most one result
     */
    fun <F, R : Any> findOneOptional(query: JoinerQuery<F, R>): OptionalMonoJoinerComposer<R> {
        markChainStarted()
        return optional(query)
    }

    /**
     * Execute a select query
     */
    fun <F, R> find(query: JoinerQuery<F, R>): FluxJoinerComposer<R> {
        markChainStarted()
        return plural(query)
    }

    /**
     * Persist a single entity, return a reference to persisted entity
     */
    fun <E : Any> persist(entity: E): MonoJoinerComposer<E> {
        markChainStarted()
        return singular(entity)
    }

    /**
     * Persist multiple entities at once, return references to persisted entities
     */
    fun <E : Any> persist(entities: List<E>): FluxJoinerComposer<E> {
        markChainStarted()
        return plural(entities)
    }

    /**
     * Creates new composer with singular (Mono) result projection
     */
    protected fun <R> singular(callback: (ENTITY_CONTAINER) -> Any): MonoJoinerComposer<R> {
        steps.add(MonoCallbackExecutionStep(callback))
        return MonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with singular (Mono) result projection
     */
    protected fun <R> singular(value: Any): MonoJoinerComposer<R> {
        steps.add(MonoComputedExecutionStep(value))
        return MonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with optional singular (Mono) result projection
     */
    protected fun <R: Any> optional(value: Any): OptionalMonoJoinerComposer<R> {
        steps.add(OptionalComputedExecutionStep(value))
        return OptionalMonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with optional singular (Mono) result projection
     */
    protected fun <R: Any> optional(callback: (ENTITY_CONTAINER) -> Any): OptionalMonoJoinerComposer<R> {
        steps.add(OptionalCallbackExecutionStep(callback))
        return OptionalMonoJoinerComposer(steps)
    }

    /**
     * Creates new composer with plural (Flux) result projection
     */
    protected fun <R> plural(callback: (ENTITY_CONTAINER) -> Any): FluxJoinerComposer<R> {
        steps.add(CallbackExecutionStep(callback))
        return FluxJoinerComposer(steps)
    }

    /**
     * Creates new composer with plural (Flux) result projection
     */
    protected fun <R> plural(value: Any): FluxJoinerComposer<R> {
        steps.add(ComputedExecutionStep(value))
        return FluxJoinerComposer(steps)
    }

    private fun markChainStarted() {
        if (isChainStarted) throw IllegalStateException("Multiple execution chains per single transaction are not supported!")
        else isChainStarted = true
    }

}