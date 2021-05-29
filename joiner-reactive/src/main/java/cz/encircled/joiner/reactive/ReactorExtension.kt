package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerExceptions
import reactor.core.Disposable
import reactor.core.publisher.FluxSink
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import java.util.*

internal object ReactorExtension {

    fun <T> MonoSink<T>.publish(result: List<T>?, error: Throwable?): Disposable = reactor {
        if (error != null) {
            error(error)
        } else when {
            result == null || result.isEmpty() -> error(JoinerExceptions.entityNotFound())
            result.size > 1 -> error(JoinerExceptions.multipleEntitiesFound())
            else -> success(result[0])
        }
    }

    fun <T> MonoSink<Optional<T>>.publishOptional(result: List<T>?, error: Throwable?): Disposable = reactor {
        if (error != null) {
            error(error)
        } else when {
            result == null || result.isEmpty() -> success(Optional.empty())
            result.size > 1 -> error(JoinerExceptions.multipleEntitiesFound())
            else -> success(Optional.ofNullable(result[0]))
        }
    }

    fun <T> FluxSink<T>.publish(result: List<T>?, error: Throwable?): Disposable = reactor {
        if (error != null) error(error)
        else {
            result?.forEach { next(it) }
            complete()
        }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    fun reactor(callback: () -> Unit): Disposable = Schedulers.boundedElastic().schedule { callback() }

}