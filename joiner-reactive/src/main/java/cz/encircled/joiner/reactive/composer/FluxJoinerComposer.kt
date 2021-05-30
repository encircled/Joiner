package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.*
import cz.encircled.joiner.reactive.ReactorExtension.publish
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class FluxJoinerComposer<ENTITY>(
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposerWithReceiver<ENTITY, List<ENTITY>, Flux<ENTITY>>(steps) {

    /**
     * Transforms the items emitted by the previous step using given synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (ENTITY) -> E): FluxJoinerComposer<E> {
        steps.add(CallbackFluxOuterScopeExecution(mapper))
        return FluxJoinerComposer(steps)
    }

    fun <E : Any> flatMap(mapper: (ENTITY) -> Mono<E>): FluxJoinerComposer<E> {
        steps.add(AsyncCallbackFluxOuterScopeExecution(mapper))
        return FluxJoinerComposer(steps)
    }

    fun filter(predicate: (ENTITY) -> Boolean): FluxJoinerComposer<ENTITY> {
        // Filter is applied to all entities, thus use MonoOuterScopeExecution to receive the [List<ENTITY>] as an input arg
        steps.add(MonoOuterScopeExecution<List<ENTITY>, List<ENTITY>> { e ->
            e.filter { predicate(it) }
        })
        return FluxJoinerComposer(steps)
    }

    fun collectToList(): MonoJoinerComposer<List<ENTITY>> {
        steps.add(MonoOuterScopeExecution<List<ENTITY>, List<ENTITY>> { e -> e })
        return MonoJoinerComposer(steps)
    }

    override fun executeChain(r: ReactorJoiner): Flux<ENTITY> = Flux.create { flux ->
        r.executeComposed(this).handle { result, error ->
            flux.publish(result, error)
        }
    }

}