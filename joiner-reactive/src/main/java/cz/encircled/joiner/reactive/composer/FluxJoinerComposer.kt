package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.ExecutionStep
import cz.encircled.joiner.reactive.FluxOuterScopeExecution
import cz.encircled.joiner.reactive.MonoOuterScopeExecution
import cz.encircled.joiner.reactive.ReactorExtension.publish
import cz.encircled.joiner.reactive.ReactorJoiner
import reactor.core.publisher.Flux

class FluxJoinerComposer<ENTITY>(
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposerWithReceiver<ENTITY, List<ENTITY>, Flux<ENTITY>>(steps) {

    /**
     * Transforms the items emitted by the previous step using given synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (ENTITY) -> E): FluxJoinerComposer<E> {
        steps.add(FluxOuterScopeExecution(mapper))
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