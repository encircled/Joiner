package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.ExecutionStep
import java.util.*

open class JoinerComposerWithReceiver<ENTITY, ENTITY_CONTAINER, PUBLISHER>(
    isMono: Boolean,
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposer<ENTITY, ENTITY_CONTAINER, PUBLISHER>(isMono, steps) {

    fun <F, R> findOne(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): MonoJoinerComposer<R> = singular {
        query(it)
    }

    fun <F, R> findOneOptional(query: (Optional<ENTITY_CONTAINER>) -> JoinerQuery<F, R>): OptionalMonoJoinerComposer<R> = optional {
        query(it)
    }

    fun <F, R> find(query: (ENTITY_CONTAINER) -> JoinerQuery<F, R>): FluxJoinerComposer<R> = plural {
        query(it)
    }

    fun <E : Any> persist(entity: (ENTITY_CONTAINER) -> E): MonoJoinerComposer<E> = singular {
        entity(it)
    }

    fun <E : Any> persistMultiple(entity: (ENTITY_CONTAINER) -> List<E>): FluxJoinerComposer<E> = plural {
        entity(it)
    }

}
