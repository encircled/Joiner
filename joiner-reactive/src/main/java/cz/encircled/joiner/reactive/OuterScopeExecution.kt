package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.reactor
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Represents a function, which must be executed in the outer scope (i.e. outside of the DB executor)
 */
interface OuterScopeExecution<E : CompletableFuture<*>> : ExecutionStep<CompletableFuture<*>>

class MonoOuterScopeMapper<T, E>(private val callback: (T) -> E) : OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<List<E>> {
        return reactor(CompletableFuture<List<E>>()) { f ->
                f.complete(listOf(callback(extractExactlyOne(arg))))
        }
    }

}

class OptionalMonoOuterScopeMapper<T, E>(private val callback: (Optional<T>) -> E) :
    OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<List<E>> {
        return reactor(CompletableFuture<List<E>>()) { f ->
            f.complete(listOf(callback(Optional.ofNullable(extractAtMostOne(arg)))))
        }
    }

}

class FluxOuterScopeMapper<T, E>(private val callback: (T) -> E) : OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<List<E>> {
        return reactor(CompletableFuture<List<E>>()) { f ->
            f.complete(arg!!.map { callback(it as T) })
        }
    }

}