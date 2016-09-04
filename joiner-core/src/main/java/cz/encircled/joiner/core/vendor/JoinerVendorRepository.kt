package cz.encircled.joiner.core.vendor

import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.EntityPath
import cz.encircled.joiner.query.join.JoinDescription

import javax.persistence.EntityManager

/**
 * Implementation is responsible for vendor-specific part of query creation logic

 * @author Kisel on 21.01.2016.
 */
interface JoinerVendorRepository {

    fun createQuery(entityManager: EntityManager): JPAQuery

    fun addJoin(query: JPAQuery, joinDescription: JoinDescription)

    fun addFetch(query: JPAQuery, joinDescription: JoinDescription, joins: Collection<JoinDescription>, rootPath: EntityPath<*>)

}
