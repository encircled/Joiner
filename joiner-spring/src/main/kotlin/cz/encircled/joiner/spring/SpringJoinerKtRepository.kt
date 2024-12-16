package cz.encircled.joiner.spring

import com.querydsl.core.types.EntityPath
import cz.encircled.joiner.kotlin.JoinerKtQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaRepositoryConfigurationAware
import kotlin.reflect.KClass

interface SpringJoinerKtRepository<T, E : EntityPath<T>> : JpaRepositoryConfigurationAware {
    fun count(query: JoinerKtQuery<T, Long, E>.() -> Any): Long
    fun findPage(pageable: Pageable, query: JoinerKtQuery<T, T, E>.() -> Any): Page<T>

    /**
     * JPA does not allow setting first/max results (HHH90003004) in a query with collection fetching. Because of that, this method creates 3 queries:
     * - find total count
     * - find only 'ids' with pagination params, while disabling associations fetching
     * - find the content by ids with associations fetching
     *
     * This assumes that an entity has 'id' field
     */
    fun <R : Any> findPage(mappingTo: KClass<R>, pageable: Pageable, query: JoinerKtQuery<T, R, E>.() -> Any): Page<R>
    fun find(query: JoinerKtQuery<T, T, E>.() -> Any): List<T>
    fun findOne(query: JoinerKtQuery<T, T, E>.() -> Any): T?
    fun getOne(query: JoinerKtQuery<T, T, E>.() -> Any): T
}
