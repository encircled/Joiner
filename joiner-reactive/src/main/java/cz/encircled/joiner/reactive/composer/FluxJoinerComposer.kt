package cz.encircled.joiner.reactive.composer

import cz.encircled.joiner.reactive.ExecutionStep
import cz.encircled.joiner.reactive.FluxOuterScopeMapper
import cz.encircled.joiner.reactive.ReactorExtension.publish
import cz.encircled.joiner.reactive.ReactorJoiner
import reactor.core.publisher.Flux

class FluxJoinerComposer<ENTITY>(
    steps: MutableList<ExecutionStep<*>>
) : JoinerComposerWithReceiver<ENTITY, List<ENTITY>, Flux<ENTITY>>(false, steps) {

    /**
     * Transforms the items emitted by the previous step using given synchronous [mapper] function.
     */
    fun <E : Any> map(mapper: (ENTITY) -> E): FluxJoinerComposer<E> = plural {
        FluxOuterScopeMapper(it, mapper)
    }

    override fun execute(r: ReactorJoiner): Flux<ENTITY> = Flux.create { flux ->
        r.executeComposed(this).handle { result, error ->
            flux.publish(result, error)
        }
    }
}