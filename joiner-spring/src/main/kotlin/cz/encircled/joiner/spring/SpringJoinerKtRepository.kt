package cz.encircled.joiner.spring

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import cz.encircled.joiner.kotlin.JoinerKtQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaRepositoryConfigurationAware
import java.util.stream.Stream
import kotlin.reflect.KClass

interface SpringJoinerKtRepository<T, E : EntityPath<T>> : JpaRepositoryConfigurationAware {
    fun count(query: JoinerKtQuery<T, Long, E>.() -> Any): Long

    /**
     * JPA does not allow setting first/max results (HHH90003004) in a query with collection fetching. Because of that, this method creates 3 queries:
     * - find total count
     * - find only 'ids' with pagination params, while disabling associations fetching
     * - find the content by ids with associations fetching
     *
     * This assumes that an entity has the 'id' field
     */
    fun findPage(pageable: Pageable, query: JoinerKtQuery<T, T, E>.() -> Any): Page<T>

    /**
     * JPA does not allow setting first/max results (HHH90003004) in a query with collection fetching. Because of that, this method creates 3 queries:
     * - find total count
     * - find only 'ids' with pagination params, while disabling associations fetching
     * - find the content by ids with associations fetching
     *
     * This assumes that an entity has the 'id' field
     */
    fun <R : Any> findPage(mappingTo: KClass<R>, pageable: Pageable, query: JoinerKtQuery<T, R, E>.() -> Any): Page<R>

    /**
     * Execute a query and return the List of results.
     */
    fun find(query: JoinerKtQuery<T, T, E>.() -> Any): List<T>

    /**
     * Execute a query and return the List of results.
     */
    fun findTuple(projection: List<Expression<*>>, query: JoinerKtQuery<T, Tuple, E>.() -> Any): List<Tuple>

    /**
     * Execute a query and stream results using JPA streaming.
     *
     * Fetch size is configured using hint `org.hibernate.fetchSize` (`HibernateHints.HINT_FETCH_SIZE`)
     *
     * Using streaming disables caching and auto-flush for the query.
     *
     * JPA streaming is supported only by Hibernate ORM.
     */
    fun findStream(query: JoinerKtQuery<T, T, E>.() -> Any): Stream<T>

    /**
     * Execute a query and return a result or null.
     */
    fun findOne(query: JoinerKtQuery<T, T, E>.() -> Any): T?

    /**
     * Execute a query and return a result, throws an error if null.
     */
    fun getOne(query: JoinerKtQuery<T, T, E>.() -> Any): T
}
