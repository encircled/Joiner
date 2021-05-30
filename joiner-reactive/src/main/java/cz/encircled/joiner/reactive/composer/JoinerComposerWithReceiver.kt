package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.ExecutionStep

// TODO flatMap
open class JoinerComposerWithReceiver<ENTITY, ENTITY_CONTAINER, PUBLISHER>(
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposer<ENTITY, ENTITY_CONTAINER, PUBLISHER>(steps) {

    open fun <F, R> findOne(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): MonoJoinerComposer<R> = singular {
        query(it)
    }

    fun <F, R> findOneOptional(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): OptionalMonoJoinerComposer<R> =
        optional {
            query(it)
        }

    open fun <F, R> find(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): FluxJoinerComposer<R> = plural {
        query(it)
    }

    fun <E : Any> persist(entity: (ENTITY_CONTAINER) -> E): MonoJoinerComposer<E> = singular {
        entity(it)
    }

    fun <E : Any> persistMultiple(entity: (ENTITY_CONTAINER) -> List<E>): FluxJoinerComposer<E> = plural {
        entity(it)
    }

}
