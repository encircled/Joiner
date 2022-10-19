package cz.encircled.joiner.kotlin

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression
import cz.encircled.joiner.query.join.JoinDescription

/**
 * List of missing stuff in Kt:
 * - distinct
 * - missing operators
 * - join (non)fetch
 * - query features
 * - groupBy/having
 */

data class PredicateContinuation<T>(
    inline val t: ((SimpleExpression<T>) -> BooleanExpression) -> BooleanExpression
)

object JoinerKtOps : ConditionOps, JoinOps {
    override var lastJoin: JoinDescription? = null
}

/**
 * @author Vlad on 05-Jun-18.
 */
object JoinerKtQueryBuilder {

    private fun <FROM_C, FROM : EntityPath<FROM_C>, PROJ> select(sf: SelectFrom<FROM_C, FROM, PROJ>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        return ExpressionJoinerKtQuery(sf.from, sf.projection, sf.isCount)
    }

    infix fun <PROJ, FROM_C, FROM : EntityPath<FROM_C>> Expression<PROJ>.from(path: FROM): JoinerKtQuery<FROM_C, PROJ, FROM> {
        return select(SelectFrom(this, path))
    }

    infix fun <PROJ : Collection<Expression<*>>, FROM_C, FROM : EntityPath<FROM_C>> PROJ.from(path: FROM): JoinerKtQuery<FROM_C, Tuple, FROM> {
        return TupleJoinerKtQuery(path, this.toTypedArray())
    }

    fun <FROM_C, FROM : EntityPath<FROM_C>> FROM.all(): JoinerKtQuery<FROM_C, FROM_C, FROM> {
        return select(SelectFrom(this, this))
    }

    fun <FROM_C, FROM : EntityPath<FROM_C>> FROM.countOf(): JoinerKtQuery<FROM_C, Long, FROM> {
        return ExpressionJoinerKtQuery(this, this, true) as JoinerKtQuery<FROM_C, Long, FROM>
    }

    private data class SelectFrom<FROM_C, FROM : EntityPath<FROM_C>, PROJ>(
        val projection: Expression<PROJ>,
        val from: FROM,
        val isCount: Boolean = false
    )

}