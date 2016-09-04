package cz.encircled.joiner.core.vendor

import com.google.common.collect.ArrayListMultimap
import com.mysema.query.JoinType
import com.mysema.query.jpa.EclipseLinkTemplates
import com.mysema.query.jpa.impl.AbstractJPAQuery
import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.EntityPath
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.join.JoinDescription
import cz.encircled.joiner.util.ReflectionUtils
import javax.persistence.EntityManager

/**
 * @author Kisel on 28.01.2016.
 */
class EclipselinkRepository : AbstractVendorRepository(), JoinerVendorRepository {

    override fun createQuery(entityManager: EntityManager): JPAQuery {
        val query = JPAQuery(entityManager, EclipseLinkTemplates.DEFAULT)
        makeInsertionOrderHints(query)
        return query
    }

    private fun makeInsertionOrderHints(sourceQuery: AbstractJPAQuery<JPAQuery>) {
        val f = ReflectionUtils.findField(AbstractJPAQuery::class.java, "hints")
        ReflectionUtils.setField(f!!, sourceQuery, ArrayListMultimap.create<Any, Any>())
    }

    override fun addFetch(query: JPAQuery, joinDescription: JoinDescription, joins: Collection<JoinDescription>, rootPath: EntityPath<*>) {
        val rootEntityAlias = rootPath.metadata.name
        val path = resolvePathToFieldFromRoot(rootEntityAlias, joinDescription, joins)

        //        if (canBeFetched(targetJoin, allJoins, path)) {
        val fetchHint = if (joinDescription.joinType == com.mysema.query.JoinType.LEFTJOIN) "eclipselink.left-join-fetch" else "eclipselink.join-fetch"
        query.setHint(fetchHint, path)
        //        }
    }

    override fun addJoin(query: JPAQuery, joinDescription: JoinDescription) {
        if (joinDescription.joinType == JoinType.RIGHTJOIN) {
            throw JoinerException("Right join is not supported in EclipseLink!")
        }

        super.addJoin(query, joinDescription)
    }

    private fun resolvePathToFieldFromRoot(rootAlias: String, targetJoinDescription: JoinDescription, joins: Collection<JoinDescription>): String? {
        // Contains two elements: current attribute and it's parent (i.e. 'group' and 'users' for "group.users")
        var holder: Array<String>?

        if (targetJoinDescription.collectionPath != null) {
            holder = targetJoinDescription.collectionPath.toString().split(DOT_ESCAPED.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else if (targetJoinDescription.singlePath != null) {
            holder = targetJoinDescription.singlePath.toString().split(DOT_ESCAPED.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        } else {
            return null
        }

        var parentSimpleName = holder[0]
        var result = holder[1]


        var i = 0
        // TODO
        holder = findJoinByEntitySimpleName(parentSimpleName, joins)
        while (holder != null && i++ < MAX_NESTED_JOIN_DEPTH) {
            parentSimpleName = holder[0]
            result = holder[1] + "." + result
            holder = findJoinByEntitySimpleName(parentSimpleName, joins)
        }

        return rootAlias + "." + result
    }

    private fun findJoinByEntitySimpleName(targetAlias: String, joins: Collection<JoinDescription>): Array<String>? {
        var result: Array<String>? = null
        for (join in joins) {
            val candidate = join.alias.metadata.name

            if (candidate == targetAlias) {
                if (join.collectionPath != null) {
                    result = join.collectionPath.toString().split(DOT_ESCAPED.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                } else if (join.singlePath != null) {
                    result = join.singlePath.toString().split(DOT_ESCAPED.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                }
                break
            }
        }
        return result
    }

    companion object {

        private val MAX_NESTED_JOIN_DEPTH = 5
        private val DOT_ESCAPED = "\\."
    }

}
