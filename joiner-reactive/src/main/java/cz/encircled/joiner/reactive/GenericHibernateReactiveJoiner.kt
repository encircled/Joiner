package cz.encircled.joiner.reactive

import com.querydsl.core.types.ParamExpression
import com.querydsl.core.types.ParamNotSetException
import com.querydsl.core.types.dsl.Param
import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.composer.JoinerComposer
import org.hibernate.reactive.stage.Stage
import org.hibernate.reactive.stage.Stage.SessionFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import javax.persistence.EntityManagerFactory

/**
 * Encapsulates Hibernate Reactive API and provides internal reactive API instead.
 *
 * Base class for other reactive implementations of Joiner
 */
abstract class GenericHibernateReactiveJoiner(val emf: EntityManagerFactory) {

    private val joiner = Joiner(emf.createEntityManager())

    private var sessionFactory: SessionFactory = emf.unwrap(SessionFactory::class.java)

    init {
        println("Creating GenericHibernateReactiveJoiner")
    }

    fun <T, C, P> executeComposed(c: JoinerComposer<T, C, P>): CompletionStage<C> {
        return sessionFactory().withTransaction { session, _ ->
            var curr = executeChainStep(false, session, c.steps[0])

            (1 until c.steps.size).forEach { i ->
                curr = curr.thenCompose { v ->
                    executeChainStep(v, session, c.steps[i])
                }
            }

            curr as CompletionStage<C>
        }
    }

    /**
     * @param value current result set
     * @param session DB session with open transaction
     * @param step chain step to be executed
     */
    protected fun executeChainStep(
        value: Any,
        session: Stage.Session,
        step: ExecutionStep<*>,
    ): CompletionStage<*> {
        return when (val stepResult = step.perform(value)) {
            // TODO this should probably be executed async and eventually returned in vertex thread
            is CompletableFuture<*> -> {
                try {
                    CompletableFuture.completedFuture(step.convertResult(stepResult.get() as List<Any>))
                } catch (e: Exception) {
                    CompletableFuture.failedFuture(e.cause)
                }
            }

            is JoinerQuery<*, *> -> createQuery(session, stepResult).resultList.thenApply {
                step.convertResult(it as List<Any>)
            }

            is List<*> -> session.persistMultiple(stepResult).thenApply {
                step.convertResult(it as List<Any>)
            }

            else -> session.persist(stepResult).thenApply {
                step.convertResult(listOf(session.getReference(stepResult)) as List<Any>)
            }
        }
    }

    protected fun <T> doPersist(entity: T): CompletionStage<T> {
        return sessionFactory().withTransaction { session, _ ->
            session.persist(entity).thenApply { session.getReference(entity) }
        }
    }

    protected fun <T> doPersistMultiple(entities: Collection<T>): CompletionStage<List<T>> {
        return sessionFactory().withTransaction { session, _ ->
            session.persistMultiple(entities)
        }
    }

    protected fun <T, R> doFind(query: JoinerQuery<T, R>): CompletionStage<List<R>> {
        return sessionFactory().withTransaction { session, _ ->
            createQuery(session, query).resultList
        }
    }

    protected fun doRemove(entity: Any): CompletionStage<Void> {
        return sessionFactory().withTransaction { session, _ ->
            session.remove(session.getReference(entity))
        }
    }

    private fun sessionFactory() : SessionFactory {
        return if (sessionFactory.isOpen) {
            sessionFactory
        } else {
            sessionFactory = emf.unwrap(SessionFactory::class.java)
            sessionFactory
        }
    }

    protected fun <R> createQuery(session: Stage.Session, query: JoinerQuery<*, R>): Stage.Query<R> {
        val queryDsl = joiner.toJPAQuery(query)
        val serializer = queryDsl.getSerializer(query.isCount)
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

    /**
     * Persist multiple entities at once
     */
    protected fun <T> Stage.Session.persistMultiple(entities: Collection<T>): CompletionStage<List<T>> {
        return persist(*entities.stream().toArray()).thenApply { entities.map { getReference(it) } }
    }

}