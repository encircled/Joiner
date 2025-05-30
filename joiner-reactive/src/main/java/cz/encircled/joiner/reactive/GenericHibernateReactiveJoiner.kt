package cz.encircled.joiner.reactive

import cz.encircled.joiner.core.JoinerJPQLSerializer
import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.reactive.composer.JoinerComposer
import jakarta.persistence.EntityManagerFactory
import org.hibernate.reactive.stage.Stage
import org.hibernate.reactive.stage.Stage.SessionFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

/**
 * Encapsulates Hibernate Reactive API and provides internal reactive API instead.
 *
 * Base class for other reactive implementations of Joiner
 */
abstract class GenericHibernateReactiveJoiner(val emf: EntityManagerFactory) {

    private val joiner = Joiner(emf.createEntityManager())

    private var sessionFactory: SessionFactory = emf.unwrap(SessionFactory::class.java)

    private val constantPrefix: String = "a"

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

    protected fun <E: Any> doRemove(entity: E): CompletionStage<E> {
        // TODO enforce jdbc thread?
        return sessionFactory().withTransaction { session, _ ->
            val reference = session.getReference(entity)
            session.remove(reference).thenApply { reference }
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
        joiner.preprocessRequestQuery(query)
        val serializer = JoinerJPQLSerializer()
        val queryString = serializer.serialize(query)
        val jpaQuery = session.createQuery<R>(queryString)

        query.limit?.apply { jpaQuery.maxResults = toInt() }
        query.offset?.apply { jpaQuery.firstResult = toInt() }

        serializer.constants.forEachIndexed { i, v ->
            jpaQuery.setParameter(i + 1, v)
        }

        return jpaQuery
    }

    /**
     * Persist multiple entities at once
     */
    protected fun <T> Stage.Session.persistMultiple(entities: Collection<T>): CompletionStage<List<T>> {
        return persist(*entities.stream().toArray()).thenApply {
            entities.map { getReference(it) }
        }
    }

}
