package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.reactor
import reactor.core.Disposable
import java.util.concurrent.CompletableFuture

/**
 * Represents a function, which must be executed in the outer scope (i.e. outside of the DB executor)
 */
interface OuterScopeExecution {

    fun perform(future: CompletableFuture<Any>): Disposable

}

class MonoOuterScopeMapper<T, E>(private val argValue: T, private val callback: (T) -> E) : OuterScopeExecution {
    override fun perform(future: CompletableFuture<Any>): Disposable = reactor {
        future.complete(listOf(callback(argValue)))
    }
}

class FluxOuterScopeMapper<T, E>(private val argValue: List<T>, private val callback: (T) -> E) : OuterScopeExecution {
    override fun perform(future: CompletableFuture<Any>): Disposable = reactor {
        future.complete(argValue.map { callback(it) })
    }
}