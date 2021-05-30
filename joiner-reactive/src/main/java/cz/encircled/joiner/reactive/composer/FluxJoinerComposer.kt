package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.*
import cz.encircled.joiner.reactive.ReactorExtension.publish
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class FluxJoinerComposer<T>(steps: MutableList<ExecutionStep<*>>) :
    JoinerComposerWithReceiver<T, List<T>, Flux<T>>(steps) {

    /**
     * Transforms the items emitted by the previous step using synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (T) -> E): FluxJoinerComposer<E> {
        steps.add(FluxCallbackOuterScopeExecution(mapper))
        return FluxJoinerComposer(steps)
    }

    /**
     * Asynchronously transforms the items emitted by the previous step using [mapper] function.
     */
    fun <E : Any> flatMap(mapper: (T) -> Mono<E>): FluxJoinerComposer<E> {
        steps.add(AsyncFluxCallbackOuterScopeExecution(mapper))
        return FluxJoinerComposer(steps)
    }

    /**
     * Test each emitted item against the [predicate], ignore items which does not pass
     */
    fun filter(predicate: (T) -> Boolean): FluxJoinerComposer<T> {
        // Filter is applied to all entities, thus use MonoOuterScopeExecution to receive the [List<ENTITY>] as an input arg
        steps.add(MonoCallbackOuterScopeExecution<List<T>, List<T>> { e ->
            e.filter { predicate(it) }
        })
        return FluxJoinerComposer(steps)
    }

    /**
     * Collect all emitted items into a [List], which is then emitted as a [Mono]
     */
    fun collectToList(): MonoJoinerComposer<List<T>> {
        steps.add(MonoCallbackOuterScopeExecution<List<T>, List<T>> { e -> e })
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Flux<T> = Flux.create { flux ->
        r.executeComposed(this).handle { result, error ->
            flux.publish(result, error)
        }
    }

}