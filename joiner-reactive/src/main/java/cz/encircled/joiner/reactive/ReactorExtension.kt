package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerExceptions
import reactor.core.Disposable
import reactor.core.publisher.FluxSink
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.util.*
import java.util.concurrent.CompletableFuture

object ReactorExtension {

    /**
     * Expect exactly one result and publish it to mono
     */
    fun <T : Any> MonoSink<T>.publish(result: List<T>?, error: Throwable?, allowNull: Boolean = false): Disposable =
        reactor(this) {
            if (error != null) {
                error(error)
            } else when {
                result.isNullOrEmpty() -> if (allowNull) success() else error(JoinerExceptions.entityNotFound())
                result.size > 1 -> error(JoinerExceptions.multipleEntitiesFound())
                else -> success(result[0])
            }
        }

    /**
     * Expect exactly one result and publish it to mono
     */
    fun <T : Any> MonoSink<T>.publish(result: T?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) {
            error(error)
        } else when (result) {
            null -> error(JoinerExceptions.entityNotFound())
            else -> success(result)
        }
    }

    /**
     * Expect at most one result and publish it to mono wrapped as [Optional]
     */
    fun <T : Any> MonoSink<Optional<T>>.publishOptional(result: Optional<T>?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) error(error)
        else success(result ?: Optional.empty<T>())
    }

    fun <T : Any> FluxSink<T>.publish(result: List<T>?, error: Throwable?): Disposable = reactor(this) {
        if (error != null) error(error)
        else {
            result?.forEach { next(it) }
            complete()
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    inline fun reactor(mono: MonoSink<*>, crossinline callback: () -> Unit): Disposable = Schedulers.boundedElastic().schedule {
        try {
            callback()
        } catch (e: Throwable) {
            mono.error(e)
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    inline fun reactor(flux: FluxSink<*>, crossinline callback: () -> Unit): Disposable = Schedulers.boundedElastic().schedule {
        try {
            callback()
        } catch (e: Throwable) {
            flux.error(e)
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    inline fun <T> reactor(crossinline callback: (CompletableFuture<T>) -> Unit): CompletableFuture<T> {
        val future = CompletableFuture<T>()
        Schedulers.boundedElastic().schedule {
            try {
                callback(future)
            } catch (e: Throwable) {
                future.completeExceptionally(e)
            }
        }

        return future
    }

    fun <T> List<T>?.getAtMostOne(): T? =
        when {
            isNullOrEmpty() -> null
            size > 1 -> throw JoinerExceptions.multipleEntitiesFound()
            else -> get(0)
        }

    fun <T> List<T>?.getExactlyOne(): T =
        when {
            isNullOrEmpty() -> throw JoinerExceptions.entityNotFound()
            size > 1 -> throw JoinerExceptions.multipleEntitiesFound()
            else -> get(0)
        }

}