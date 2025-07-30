package cz.encircled.joiner.kotlin

import com.querydsl.core.Tuple
import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.query.JoinerQueryBase
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.QueryFeature
import cz.encircled.joiner.query.join.JoinDescription

class ExpressionJoinerKtQuery<FROM_C, PROJ, FROM : EntityPath<FROM_C>>(
    private val entityPath: FROM,
    projection: Expression<PROJ>,
    isCount: Boolean,
    delegate: JoinerQuery<FROM_C, PROJ> = when {
        (isCount) -> Q.count(entityPath) as JoinerQuery<FROM_C, PROJ>
        else -> Q.select(projection).from(entityPath)
    }
) : JoinerKtQuery<FROM_C, PROJ, FROM>(entityPath, delegate)

class TupleJoinerKtQuery<FROM_C, FROM : EntityPath<FROM_C>>(
    private val entityPath: FROM,
    projection: Array<Expression<*>>,
    delegate: JoinerQuery<FROM_C, Tuple> = Q.select(*projection).from(entityPath)
) : JoinerKtQuery<FROM_C, Tuple, FROM>(entityPath, delegate)

open class JoinerKtQuery<FROM_C, PROJ, FROM : EntityPath<FROM_C>>(
    private val entityPath: FROM,
    internal val delegate: JoinerQuery<FROM_C, PROJ>
) : JoinerQuery<FROM_C, PROJ> by delegate, JoinOps {

    override var lastJoin: JoinDescription? = null

    infix fun where(where: (e: FROM) -> Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.where(where.invoke(entityPath))
        return this
    }

    infix fun andWhere(where: (e: FROM) -> BooleanExpression): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.andWhere(where.invoke(entityPath))
        return this
    }

    infix fun orWhere(where: (e: FROM) -> BooleanExpression): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.orWhere(where.invoke(entityPath))
        return this
    }

    override infix fun where(where: Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.where(where)
        return this
    }

    override infix fun andWhere(where: BooleanExpression): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.andWhere(where)
        return this
    }

    override infix fun orWhere(where: BooleanExpression): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.orWhere(where)
        return this
    }

    infix fun feature(feature: QueryFeature): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.addFeatures(feature)
        return this
    }

    infix fun features(features: Collection<QueryFeature>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.addFeatures(features)
        return this
    }

    override infix fun limit(limit: Int?): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.limit(limit)
        return this
    }

    override infix fun offset(offset: Int?): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.offset(offset)
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

    infix fun groupBy(groupBy: Expression<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.groupBy(groupBy)
        return this
    }

    infix fun groupBy(groupBy: List<Expression<*>>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.groupBy(*groupBy.toTypedArray())
        return this
    }

    override infix fun having(having: Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.having(having)
        return this
    }

    override infix fun distinct(isDistinct: Boolean): JoinerQuery<FROM_C, PROJ> {
        delegate.distinct(isDistinct)
        return this
    }

    fun count(): JoinerKtQuery<FROM_C, PROJ, FROM> {
        (delegate as JoinerQueryBase).count()
        return this
    }

}