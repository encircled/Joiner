package cz.encircled.joiner.kotlin.reactive

import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.GenericHibernateReactiveJoiner
import cz.encircled.joiner.reactive.ReactorExtension.getExactlyOne
import kotlinx.coroutines.future.await
import javax.persistence.EntityManagerFactory

class KtReactiveJoiner(emf: EntityManagerFactory) : GenericHibernateReactiveJoiner(emf) {

    suspend fun <F, R> find(query: JoinerQuery<F, R>): List<R> = doFind(query).await()

    suspend fun <F, R> findOne(query: JoinerQuery<F, R>): R = doFind(query).await().getExactlyOne()

    suspend fun <E> persist(entity: E): E = doPersist(entity).await()

    suspend fun <E> persist(entities: List<E>): List<E> = doPersistMultiple(entities).await()

    suspend fun <E : Any> remove(entity: E): Void = doRemove(entity).await()

}