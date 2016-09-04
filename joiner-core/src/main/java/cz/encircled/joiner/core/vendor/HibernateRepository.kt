package cz.encircled.joiner.core.vendor

import com.mysema.query.jpa.HQLTemplates
import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.EntityPath
import cz.encircled.joiner.query.join.JoinDescription

import javax.persistence.EntityManager

/**
 * @author Kisel on 21.01.2016.
 */
class HibernateRepository : AbstractVendorRepository(), JoinerVendorRepository {

    override fun createQuery(entityManager: EntityManager): JPAQuery {
        return JPAQuery(entityManager, HQLTemplates.DEFAULT)
    }

    override fun addFetch(query: JPAQuery, joinDescription: JoinDescription, joins: Collection<JoinDescription>, rootPath: EntityPath<*>) {
        query.fetch()
    }
}
