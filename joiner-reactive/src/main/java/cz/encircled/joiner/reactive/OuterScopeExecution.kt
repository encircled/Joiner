package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.getExactlyOne
import cz.encircled.joiner.reactive.ReactorExtension.reactor
import java.util.concurrent.CompletableFuture

/**
 * An [ExecutionStep], which is computed in the outer scope (i.e. outside of the DB threads)
 *
 * @see ExecutionStep
 */
interface OuterScopeExecution<E> : ExecutionStep<CompletableFuture<List<E>>>

class MonoOuterScopeExecution<T, E>(private val callback: (T) -> E) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        f.complete(listOf(callback(arg as T)))
    }

    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()

}

class SyncMonoOuterScopeExecution<E>(private val data: E) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> {
        return CompletableFuture.completedFuture(listOf(data))
    }

    override fun convertResult(arg: List<Any>): Any = arg.getExactlyOne()

}


class FluxOuterScopeExecution<T, E>(private val callback: (T) -> E) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> = reactor { f ->
        f.complete((arg as List<T>).map { callback(it) })
    }

}

class SyncFluxOuterScopeExecution<E>(private val data: List<E>) : OuterScopeExecution<E> {

    override fun perform(arg: Any): CompletableFuture<List<E>> {
        return CompletableFuture.completedFuture(data)
    }

}
