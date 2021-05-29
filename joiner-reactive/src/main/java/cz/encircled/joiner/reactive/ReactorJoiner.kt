package cz.encircled.joiner.reactive

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.ReactorExtension.publish
import cz.encircled.joiner.reactive.ReactorExtension.reactor
import cz.encircled.joiner.reactive.composer.JoinerComposer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.persistence.EntityManagerFactory

/**
 * Implementation of Joiner with Project Reactor API
 */
class ReactorJoiner(emf: EntityManagerFactory) : GenericHibernateReactiveJoiner(emf) {

    /**
     * Execute multiple statements in a single DB transaction
     */
    fun <T, P> transaction(init: JoinerComposer<*, *, *>.() -> JoinerComposer<T, *, P>): P {
        return JoinerComposer<T, T, P>(true, ArrayList()).init().execute(this)
    }

    /**
     * Persist a single entity in transaction, returns Mono with a reference to persisted entity
     */
    fun <T> persist(entity: T): Mono<T> = Mono.create { mono ->
        doPersist(entity).handle { result, error ->
            reactor(mono) {
                if (error != null) mono.error(error) else mono.success(result)
            }
        }
    }

    /**
     * Persist multiple entities in a transaction, returns Flux with references to persisted entities
     */
    fun <T> persist(entities: Collection<T>): Flux<T> = Flux.create { flux ->
        doPersistMultiple(entities).handle { result, error -> flux.publish(result, error) }
    }

    /**
     * Execute a select and expect exactly one result, returned as a Mono
     */
    fun <T, R> findOne(query: JoinerQuery<T, R>): Mono<R> = Mono.create { mono ->
        doFind(query).handle { result, error -> mono.publish(result, error) }
    }

/*    *//**
     * Execute a select and expect at most one result, returned as a Mono
     * TODO test
     *//*
    fun <T, R> findOneOptional(query: JoinerQuery<T, R>): Mono<Optional<R>> = Mono.create { mono ->
        doFind(query).handle { result, error -> mono.publishOptional(result, error) }
    }*/

    /**
     * Execute a select, returns result set as a Flux
     */
    fun <T, R> find(query: JoinerQuery<T, R>): Flux<R> = Flux.create { flux ->
        doFind(query).handle { result, error -> flux.publish(result, error) }
    }

    fun remove(entity: Any): Mono<Boolean> = Mono.create { mono ->
        doRemove(entity).handle { _, error -> mono.publish(listOf(true), error) }
    }

}