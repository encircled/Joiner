package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerExceptions
import reactor.core.Disposable
import reactor.core.publisher.FluxSink
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.CompletableFuture

internal object ReactorExtension {

    fun <T> MonoSink<T>.publish(result: List<T>?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) {
            error(error)
        } else when {
            result == null || result.isEmpty() -> error(JoinerExceptions.entityNotFound())
            result.size > 1 -> error(JoinerExceptions.multipleEntitiesFound())
            else -> success(result[0])
        }
    }

    fun <T> MonoSink<Optional<T>>.publishOptional(result: List<T>?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) {
            error(error)
        } else when {
            result == null || result.isEmpty() -> success(Optional.empty())
            result.size > 1 -> error(JoinerExceptions.multipleEntitiesFound())
            else -> success(Optional.ofNullable(result[0]))
        }
    }

    fun <T> FluxSink<T>.publish(result: List<T>?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) error(error)
        else {
            result?.forEach { next(it) }
            complete()
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    fun reactor(mono: MonoSink<*>, callback: () -> Unit): Disposable = Schedulers.boundedElastic().schedule {
        try {
            callback()
        } catch (e: Throwable) {
            mono.error(e)
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    fun reactor(flux: FluxSink<*>, callback: () -> Unit): Disposable = Schedulers.boundedElastic().schedule {
        try {
            callback()
        } catch (e: Throwable) {
            flux.error(e)
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    fun <T> reactor(future: CompletableFuture<T>, callback: (CompletableFuture<T>) -> Unit): CompletableFuture<T> {
        Schedulers.boundedElastic().schedule {
            try {
                callback(future)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

}