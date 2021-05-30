package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.ExecutionStep

open class JoinerComposerWithReceiver<ENTITY, ENTITY_CONTAINER, PUBLISHER>(
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposer<ENTITY, ENTITY_CONTAINER, PUBLISHER>(steps) {

    /**
     * Execute a select query and expect exactly one result
     */
    open fun <F, R> findOne(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): MonoJoinerComposer<R> = singular {
        query(it)
    }

    /**
     * Execute a select query and expect at most one result
     */
    fun <F, R> findOneOptional(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): OptionalMonoJoinerComposer<R> =
        optional {
            query(it)
        }

    /**
     * Execute a select query
     */
    open fun <F, R> find(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): FluxJoinerComposer<R> = plural {
        query(it)
    }

    /**
     * Persist a single entity, return a reference to persisted entity
     */
    fun <E : Any> persist(entity: (ENTITY_CONTAINER) -> E): MonoJoinerComposer<E> = singular {
        entity(it)
    }

    /**
     * Persist multiple entities at once, return references to persisted entities
     */
    fun <E : Any> persistMultiple(entity: (ENTITY_CONTAINER) -> List<E>): FluxJoinerComposer<E> = plural {
        entity(it)
    }

}
