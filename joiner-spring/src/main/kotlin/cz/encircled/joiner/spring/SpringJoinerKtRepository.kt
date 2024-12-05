package cz.encircled.joiner.spring

import com.querydsl.core.types.EntityPath
import cz.encircled.joiner.kotlin.JoinerKtQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.JpaRepositoryConfigurationAware

interface SpringJoinerKtRepository<T, E : EntityPath<T>> : JpaRepositoryConfigurationAware {
    fun count(query: JoinerKtQuery<T, Long, E>.() -> Any): Long
    fun find(pageable: Pageable, query: JoinerKtQuery<T, T, E>.() -> Any): Page<T>
    fun find(query: JoinerKtQuery<T, T, E>.() -> Any): List<T>
    fun findOne(query: JoinerKtQuery<T, T, E>.() -> Any): T?
    fun getOne(query: JoinerKtQuery<T, T, E>.() -> Any): T
}
