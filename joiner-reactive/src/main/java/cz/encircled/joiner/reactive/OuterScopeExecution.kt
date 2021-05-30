package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.getExactlyOne
import cz.encircled.joiner.reactive.ReactorExtension.reactor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

/**
 * An [ExecutionStep], which is computed in the outer scope (i.e. outside of the DB threads)
 *
 * @see ExecutionStep
 */
interface OuterScopeExecution<E> : ExecutionStep<CompletableFuture<List<E>>>

class MonoCallbackOuterScopeExecution<T, E>(private val callback: (T) -> E) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        f.complete(listOf(callback(arg as T)))
    }

    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()

}

class AsyncMonoCallbackOuterScopeExecution<T, E>(private val callback: (T) -> Mono<E>) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        callback(arg as T)
            .doOnError {
                f.completeExceptionally(it)
            }
            .subscribe { result ->
                f.complete(listOf(result))
            }
    }

    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()

}


class FluxCallbackOuterScopeExecution<T, E>(private val callback: (T) -> E) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        f.complete((arg as List<T>).map { callback(it) })
    }

}

class AsyncFluxCallbackOuterScopeExecution<T, E>(private val callback: (T) -> Mono<E>) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        Flux.fromStream((arg as List<T>).stream())
            .flatMap(callback)
            .collectList()
            .subscribe { result ->
                f.complete(result)
            }
    }

}
