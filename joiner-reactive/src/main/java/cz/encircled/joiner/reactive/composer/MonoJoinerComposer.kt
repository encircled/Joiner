package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.ExecutionStep
import cz.encircled.joiner.reactive.MonoOuterScopeExecution
import cz.encircled.joiner.reactive.ReactorExtension.publish
import cz.encircled.joiner.reactive.ReactorExtension.publishOptional
import cz.encircled.joiner.reactive.ReactorJoiner
import reactor.core.publisher.Mono
import java.util.*

class MonoJoinerComposer<ENTITY>(
    steps: MutableList<ExecutionStep<*>>,
) : JoinerComposerWithReceiver<ENTITY, ENTITY, Mono<ENTITY>>(steps) {

    /**
     * Transforms the item emitted by the previous step using given synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (ENTITY) -> E): MonoJoinerComposer<E> {
        steps.add(MonoOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Mono<ENTITY> = Mono.create { mono ->
        r.executeComposed(this).handle { result, error ->
            mono.publish(result, error)
        }
    }

}

class OptionalMonoJoinerComposer<ENTITY>(
    steps: MutableList<ExecutionStep<*>>,
) : JoinerComposerWithReceiver<ENTITY, Optional<ENTITY>, Mono<Optional<ENTITY>>>(steps) {

    /**
     * Transforms the item emitted by the previous step using given synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (Optional<ENTITY>) -> E): MonoJoinerComposer<E> {
        steps.add(MonoOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Mono<Optional<ENTITY>> = Mono.create { mono ->
        r.executeComposed(this).handle { result, error ->
            mono.publishOptional(result, error)
        }
    }

}