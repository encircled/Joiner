package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.JoinerQuery
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.persistence.EntityManagerFactory

/**
 * Implementation of Joiner with Project Reactor API
 */
class ReactorJoiner(emf: EntityManagerFactory) : GenericHibernateReactiveJoiner(emf) {

    /**
     * Persist a single entity in transaction, returns Mono with a reference to persisted entity
     */
    fun <T> persist(entity: T): Mono<T> {
        return Mono.create { mono ->
            doPersist(entity,
                onSuccess = { mono.success(it) },
                onError = { mono.error(it) },
                onComplete = { mono.success() }
            )
        }
    }

    /**
     * Persist multiple entities in a transaction, returns Flux with references to persisted entities
     */
    fun <T> persist(entities: Collection<T>): Flux<T> {
        return Flux.create { flux ->
            doPersistMultiple(entities,
                onSuccess = { flux.next(it) },
                onError = { flux.error(it) },
                onComplete = { flux.complete() }
            )
        }
    }

    /**
     * Execute a select and expect at most one result, returned as a Mono
     */
    fun <T, R> findOne(query: JoinerQuery<T, R>): Mono<R> {
        return Mono.create { mono ->
            var result: R? = null

            doFind(query,
                onNext = {
                    if (result == null) {
                        result = it
                    } else {
                        mono.error(JoinerException("FindOne returned multiple results!"))
                        return@doFind
                    }
                },
                onError = { mono.error(it) },
                onComplete = {
                    if (result != null) mono.success(result) else mono.error(JoinerException("Entity not found"))
                }
            )
        }
    }

    /**
     * Execute a select, returns result set as a Flux
     */
    fun <T, R> find(query: JoinerQuery<T, R>): Flux<R> {
        return Flux.create { flux ->
            doFind(query,
                onNext = { flux.next(it) },
                onError = { flux.error(it) },
                onComplete = { flux.complete() }
            )
        }
    }

}