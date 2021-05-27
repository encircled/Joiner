package cz.encircled.joiner.reactive

import com.querydsl.core.types.ParamExpression
import com.querydsl.core.types.ParamNotSetException
import com.querydsl.core.types.dsl.Param
import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.JoinerQuery
import org.hibernate.reactive.stage.Stage
import java.util.concurrent.CompletionStage
import javax.persistence.EntityManagerFactory

/**
 * Encapsulates Hibernate Reactive API and provides internal reactive API instead.
 *
 * Base class for other reactive implementations of Joiner
 */
abstract class GenericHibernateReactiveJoiner(emf: EntityManagerFactory) {

    private val joiner = Joiner(emf.createEntityManager())

    private val sessionFactory: Stage.SessionFactory = emf.unwrap(Stage.SessionFactory::class.java)

    fun executeComposed(a: List<(Any?) -> Any>): CompletionStage<*> {
        val toStage: (Any?, Stage.Session, (Any?) -> Any) -> CompletionStage<*> = { value, session, actionCreator ->
            val action = actionCreator(value)
            if (action is JoinerQuery<*, *>) {
                createQuery(session, action).resultList
            } else session.persist(action).handle { _, _ ->
                session.getReference(action)
            }
        }

        return sessionFactory.withTransaction { session, t ->
            var curr = toStage(null, session, a[0])
            (1 until a.size).forEach { i ->
                curr = curr.thenCompose { v ->
                    toStage(v, session, a[i])
                }
            }
            curr
        }
    }

    protected fun <T> doPersist(entity: T) : CompletionStage<T> {
        return sessionFactory.withTransaction { session, _ ->
                session.persist(entity).thenApply { session.getReference(entity) }
        }
    }

    protected fun <T> doPersistMultiple(entities: Collection<T>) : CompletionStage<List<T>> {
        return sessionFactory.withTransaction { session, _ ->
            val asArray = entities.stream().toArray()
            session.persist(*asArray).thenApply { entities.map { e -> session.getReference(e) } }
        }
    }

    protected fun <T, R> doFind(query: JoinerQuery<T, R>) : CompletionStage<List<R>> {
        return sessionFactory.withTransaction { session, _ ->
            createQuery(session, query).resultList
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