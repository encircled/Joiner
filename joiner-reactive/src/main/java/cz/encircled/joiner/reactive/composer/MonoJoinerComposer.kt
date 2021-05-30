package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.AsyncMonoCallbackOuterScopeExecution
import cz.encircled.joiner.reactive.ExecutionStep
import cz.encircled.joiner.reactive.MonoCallbackOuterScopeExecution
import cz.encircled.joiner.reactive.ReactorExtension.publish
import cz.encircled.joiner.reactive.ReactorExtension.publishOptional
import cz.encircled.joiner.reactive.ReactorJoiner
import reactor.core.publisher.Mono
import java.util.*

class MonoJoinerComposer<T>(steps: MutableList<ExecutionStep<*>>) : JoinerComposerWithReceiver<T, T, Mono<T>>(steps) {

    /**
     * Transforms the item emitted by the previous step using synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (T) -> E): MonoJoinerComposer<E> {
        steps.add(MonoCallbackOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    /**
     * Asynchronously transforms the item emitted by the previous step using [mapper] function.
     */
    fun <E : Any> flatMap(mapper: (T) -> Mono<E>): MonoJoinerComposer<E> {
        steps.add(AsyncMonoCallbackOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Mono<T> = Mono.create { mono ->
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
        steps.add(MonoCallbackOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    fun <E : Any> flatMap(mapper: (Optional<ENTITY>) -> Mono<E>): MonoJoinerComposer<E> {
        steps.add(AsyncMonoCallbackOuterScopeExecution(mapper))
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Mono<Optional<ENTITY>> = Mono.create { mono ->
        r.executeComposed(this).handle { result, error ->
            mono.publishOptional(result, error)
        }
    }

}