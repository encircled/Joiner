package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.reactor
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Represents a function, which must be executed in the outer scope (i.e. outside of the DB executor)
 */
interface OuterScopeExecution<E : CompletableFuture<*>> : ExecutionStep<CompletableFuture<*>>

class MonoOuterScopeMapper<T, E>(private val callback: (T) -> E) : OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<*> {
        val future = CompletableFuture<List<E>>()
        reactor {
            try {
                future.complete(listOf(callback(extractExactlyOne(arg))))
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

}

class OptionalMonoOuterScopeMapper<T, E>(private val callback: (Optional<T>) -> E) : OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<*> {
        val future = CompletableFuture<List<E>>()
        reactor {
            try {
                future.complete(listOf(callback(Optional.ofNullable(extractAtMostOne(arg)))))
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }
        return future
    }

}

class FluxOuterScopeMapper<T, E>(private val callback: (T) -> E) : OuterScopeExecution<CompletableFuture<List<E>>> {

    override fun perform(arg: List<Any>?): CompletableFuture<*> {
        val future = CompletableFuture<List<E>>()
        reactor {
            future.complete(arg!!.map { callback(it as T) })
        }
        return future
    }

}