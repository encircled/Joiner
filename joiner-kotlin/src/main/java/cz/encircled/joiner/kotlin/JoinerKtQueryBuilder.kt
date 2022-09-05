package cz.encircled.joiner.kotlin

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Path
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.QueryFeature

/**
 * List of missing stuff in Kt:
 * - distinct
 * - missing operators
 * - join (non)fetch
 * - query features
 */

data class PredicateContinuation<T>(
    inline val t: ((SimpleExpression<T>) -> BooleanExpression) -> BooleanExpression
)

open class KConditionOps : ConditionOps


object JoinerKtOps : ConditionOps, JoinOps

class JoinerKtQuery<FROM_C, PROJ, FROM : EntityPath<FROM_C>>(
    private val entityPath: FROM,
    projection: Path<PROJ>,
    isCount: Boolean,
    internal val delegate: JoinerQuery<FROM_C, PROJ> = if (isCount) Q.count(entityPath) as JoinerQuery<FROM_C, PROJ> else Q.select(
        projection
    ).from(entityPath)
) : JoinerQuery<FROM_C, PROJ> by delegate, JoinOps {

    infix fun where(where: ConditionOps.(e: FROM) -> Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.where(where.invoke(KConditionOps(), entityPath))
        return this
    }

    infix fun feature(feature: QueryFeature): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.addFeatures(feature)
        return this
    }

    override infix fun limit(limit: Long?): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.limit(limit)
        return this
    }

    override infix fun offset(offset: Long?): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.offset(limit)
        return this
    }

    infix fun asc(asc: (e: FROM) -> Expression<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.asc(asc.invoke(entityPath))
        return this
    }

    override infix fun asc(asc: Expression<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.asc(asc)
        return this
    }

    infix fun desc(desc: (e: FROM) -> Expression<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.desc(desc.invoke(entityPath))
        return this
    }

    override infix fun desc(desc: Expression<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.desc(desc)
        return this
    }

}

/**
 * @author Vlad on 05-Jun-18.
 */
object JoinerKtQueryBuilder : JoinOps {

    private fun <FROM_C, FROM : EntityPath<FROM_C>, PROJ> select(sf: SelectFrom<FROM_C, FROM, PROJ>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        return JoinerKtQuery(sf.from, sf.projection, sf.isCount)
    }

    infix fun <PROJ, FROM_C, FROM : EntityPath<FROM_C>> Path<PROJ>.from(path: FROM): JoinerKtQuery<FROM_C, PROJ, FROM> {
        return select(SelectFrom(this, path))
    }

    fun <FROM_C, FROM : EntityPath<FROM_C>> FROM.all(): JoinerKtQuery<FROM_C, FROM_C, FROM> {
        return select(SelectFrom(this, this))
    }

    fun <FROM_C, FROM : EntityPath<FROM_C>> FROM.countOf(): JoinerKtQuery<FROM_C, Long, FROM> {
        return JoinerKtQuery(this, this, true) as JoinerKtQuery<FROM_C, Long, FROM>
    }

    private data class SelectFrom<FROM_C, FROM : EntityPath<FROM_C>, PROJ>(
        val projection: Path<PROJ>,
        val from: FROM,
        val isCount: Boolean = false
    )

}