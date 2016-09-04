package cz.encircled.joiner.core.vendor

import com.mysema.query.JoinType
import com.mysema.query.jpa.impl.JPAQuery
import com.mysema.query.types.CollectionExpression
import com.mysema.query.types.EntityPath
import com.mysema.query.types.Path
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.join.JoinDescription

/**
 * @author Kisel on 28.01.2016.
 */
abstract class AbstractVendorRepository : JoinerVendorRepository {

    @Suppress("unchecked_cast")
    override fun addJoin(query: JPAQuery, joinDescription: JoinDescription) {
        val alias = joinDescription.alias as Path<Any>

        when (joinDescription.joinType) {
            JoinType.LEFTJOIN -> if (joinDescription.isCollectionPath) {
                val collectionPath = joinDescription.collectionPath as CollectionExpression<*, Any>
                query.leftJoin(collectionPath, alias)
            } else {
                val singlePath = joinDescription.singlePath as EntityPath<Any>
                query.leftJoin(singlePath, alias)
            }
            JoinType.INNERJOIN -> if (joinDescription.isCollectionPath) {
                val collectionPath = joinDescription.collectionPath as CollectionExpression<*, Any>
                query.innerJoin(collectionPath, alias)
            } else {
                val singlePath = joinDescription.singlePath as EntityPath<Any>
                query.innerJoin(singlePath, alias)
            }
            JoinType.RIGHTJOIN -> if (joinDescription.isCollectionPath) {
                val collectionPath = joinDescription.collectionPath as CollectionExpression<*, Any>
                query.rightJoin(collectionPath, alias)
            } else {
                val singlePath = joinDescription.singlePath as EntityPath<Any>
                query.rightJoin(singlePath, alias)
            }
            else -> throw JoinerException("Join type " + joinDescription.joinType + " is not supported!")
        }

        if (joinDescription.on != null) {
            query.on(joinDescription.on)
        }
    }

}
