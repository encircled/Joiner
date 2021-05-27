package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.JoinerQuery
import reactor.core.CorePublisher
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import reactor.core.scheduler.Schedulers
import javax.persistence.EntityManagerFactory

/**
 * Implementation of Joiner with Project Reactor API
 */
class ReactorJoiner(emf: EntityManagerFactory) : GenericHibernateReactiveJoiner(emf) {

    /**
     * Execute multiple statements in a single DB transaction
     */
    fun <T, R : CorePublisher<T>> transaction(init: JoinerComposer<Void, Mono<Void>>.() -> JoinerComposer<T, R>): R {
        val composer: JoinerComposer<T, R> = JoinerComposer<Void, Mono<Void>>(true, ArrayList()).init()

        return if (composer.isMono) {
            Mono.create<T> { mono ->
                executeComposed(composer.composed).handle { result, error ->
                    (result as List<T>).toMono(mono, error)
                }
            } as R
        } else {
            Flux.create<T> { flux ->
                executeComposed(composer.composed).handle { result, error ->
                    (result as List<T>).toFlux(flux, error)
                }
            } as R
        }
    }

    /**
     * Persist a single entity in transaction, returns Mono with a reference to persisted entity
     */
    fun <T> persist(entity: T): Mono<T> = Mono.create { mono ->
        doPersist(entity).handle { result, error ->
            reactor {
                if (error != null) mono.error(error) else mono.success(result)
            }
        }
    }

    /**
     * Persist multiple entities in a transaction, returns Flux with references to persisted entities
     */
    fun <T> persist(entities: Collection<T>): Flux<T> = Flux.create { flux ->
        doPersistMultiple(entities).handle { result, error -> result.toFlux(flux, error) }
    }

    /**
     * Execute a select and expect at most one result, returned as a Mono
     */
    fun <T, R> findOne(query: JoinerQuery<T, R>): Mono<R> = Mono.create { mono ->
        doFind(query).handle { result, error -> result.toMono(mono, error) }
    }

    /**
     * Execute a select, returns result set as a Flux
     */
    fun <T, R> find(query: JoinerQuery<T, R>): Flux<R> = Flux.create { flux ->
        doFind(query).handle { result, error -> result.toFlux(flux, error) }
    }

    /**
     * Execute given `callback` in Reactor scope
     */
    private fun reactor(callback: () -> Unit) = Schedulers.boundedElastic().schedule { callback() }

    private fun <T> List<T>.toMono(to: MonoSink<T>, error: Throwable?) = reactor {
        if (error != null) to.error(error)
        else when {
            isEmpty() -> to.error(JoinerException("Entity not found"))
            size > 1 -> to.error(JoinerException("FindOne returned multiple results!"))
            else -> to.success(get(0))
        }
    }

    private fun <T> List<T>.toFlux(to: FluxSink<T>, error: Throwable?) = reactor {
        if (error != null) to.error(error)
        else {
            forEach { to.next(it) }
            to.complete()
        }
    }

    class JoinerComposer<T, P : CorePublisher<*>>(val isMono: Boolean, val composed: MutableList<(Any?) -> Any>) {

        fun <F, R> findOne(query: (T) -> JoinerQuery<F, R>): JoinerComposer<R, Mono<R>> {
            composed.add { query(it as T) }
            return JoinerComposer(true, composed)
        }

        fun <F, R> find(query: (T) -> JoinerQuery<F, R>): JoinerComposer<R, Flux<R>> {
            composed.add { query(it as T) }
            return JoinerComposer(false, composed)
        }

        fun <E : Any> persist(entity: E): JoinerComposer<E, Mono<E>> {
            composed.add { entity }
            return JoinerComposer(true, composed)
        }

        fun <E : Any> persist(entity: List<E>): JoinerComposer<E, Mono<E>> {
            TODO("Not implemented ;(")
        }

        fun <E : Any> persist(entity: (T) -> E): JoinerComposer<E, Mono<E>> {
            composed.add { entity(it as T) }
            return JoinerComposer(true, composed)
        }

        fun <E : Any> persistMultiple(entity: (T) -> List<E>): JoinerComposer<E, Mono<E>> {
            TODO("Not implemented ;(")
        }

    }

}