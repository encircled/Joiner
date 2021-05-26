package cz.encircled.joiner.reactive

import com.querydsl.core.types.ParamExpression
import com.querydsl.core.types.ParamNotSetException
import com.querydsl.core.types.dsl.Param
import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.JoinerQuery
import org.hibernate.reactive.stage.Stage
import reactor.core.scheduler.Schedulers
import javax.persistence.EntityManagerFactory

/**
 * Encapsulates Hibernate Reactive API and provides internal reactive API instead.
 *
 * Base class for other reactive implementations of Joiner
 */
abstract class GenericHibernateReactiveJoiner(emf: EntityManagerFactory) {

    private val joiner = Joiner(emf.createEntityManager())

    private val sessionFactory: Stage.SessionFactory = emf.unwrap(Stage.SessionFactory::class.java)

    protected fun <T> doPersist(
        entity: T,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ) {
        sessionFactory.withTransaction { session, _ ->
            try {
                session.persist(entity).handle { _, error ->
                    Schedulers.boundedElastic().schedule {
                        if (error != null) {
                            onError(error)
                        } else {
                            onSuccess(session.getReference(entity))
                            onComplete()
                        }
                    }
                }
            } catch (e: Exception) {
                onError(e)
                return@withTransaction null
            }
        }
    }

    protected fun <T> doPersistMultiple(
        entities: Collection<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ) {
        sessionFactory.withTransaction { session, _ ->
            try {
                val asArray = entities.stream().toArray()
                session.persist(*asArray).handle { _, error ->
                    Schedulers.boundedElastic().schedule {
                        if (error != null) {
                            onError(error)
                        } else {
                            entities.forEach {
                                onSuccess(session.getReference(it))
                            }
                            onComplete()
                        }
                    }
                }
            } catch (e: Exception) {
                onError(e)
                return@withTransaction null
            }
        }
    }

    protected fun <T, R> doFind(
        query: JoinerQuery<T, R>,
        onNext: (R) -> Unit,
        onError: (Throwable) -> Unit,
        onComplete: () -> Unit
    ) {
        sessionFactory.withTransaction { session, _ ->
            val jpa = createQuery(session, query)

            jpa.resultList.handle { t, u ->
                Schedulers.boundedElastic().schedule {
                    if (u?.cause != null) {
                        onError(u.cause!!)
                    } else {
                        t.forEach {
                            onNext(it)
                        }
                    }
                    onComplete()
                    session.close()
                }
            }
        }
    }

    protected fun <R> createQuery(session: Stage.Session, query: JoinerQuery<*, R>): Stage.Query<R> {
        val queryDsl = joiner.toJPAQuery(query)
        val serializer = queryDsl.serializer
        val jpaQuery = session.createQuery<R>(serializer.toString())

        query.limit?.apply { jpaQuery.setMaxResults(toInt()) }
        query.offset?.apply { jpaQuery.setFirstResult(toInt()) }

        setConstants(jpaQuery, serializer.constantToAllLabels, queryDsl.metadata.params)

        return jpaQuery
    }

    protected fun setConstants(
        query: Stage.Query<*>,
        constants: Map<Any?, String>,
        params: Map<ParamExpression<*>?, Any?>
    ) {
        for (entry in constants.entries) {
            val key = entry.value
            var value = entry.key
            if (Param::class.java.isInstance(value)) {
                value = params[value]
                if (value == null) {
                    throw ParamNotSetException(entry.key as Param<*>?)
                }
            }

            query.setParameter(Integer.valueOf(key), value)
        }
    }

}