package cz.encircled.joiner.kotlin

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Path
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription

/**
 * List of missing stuff in Kt:
 * - distinct
 * - some operators
 * - join (non)fetch
 * - query features
 */

data class PredicateContinuation<T>(
    inline val t: ((SimpleExpression<T>) -> BooleanExpression) -> BooleanExpression
)

open class KConditionOps : ConditionOps

interface ConditionOps {

    infix fun StringExpression.contains(to: String): BooleanExpression = contains(to)

    infix fun <T> SimpleExpression<T>.eq(to: T): BooleanExpression = eq(to)

    infix fun <T> SimpleExpression<T>.isIn(to: Collection<T>): BooleanExpression = `in`(to)

    infix fun <T> SimpleExpression<T>.notIn(to: Collection<T>): BooleanExpression = notIn(to)

    infix fun <T> SimpleExpression<T>.ne(to: T): BooleanExpression = ne(to)

    infix fun <T> PredicateContinuation<T>.eq(to: T): BooleanExpression {
        return t.invoke { it.eq(to) }
    }

    infix fun <T> PredicateContinuation<T>.ne(to: T): BooleanExpression {
        return t.invoke { it.ne(to) }
    }

    infix fun <T> PredicateContinuation<T>.isIn(to: Collection<T>): BooleanExpression {
        return t.invoke { it.`in`(to) }
    }

    infix fun <T> PredicateContinuation<T>.notIn(to: Collection<T>): BooleanExpression {
        return t.invoke { it.notIn(to) }
    }

    infix fun <T> BooleanExpression.and(exp: SimpleExpression<T>): PredicateContinuation<T> {
        return PredicateContinuation { this.and(it.invoke(exp)) }
    }

    infix fun <T> BooleanExpression.or(exp: SimpleExpression<T>): PredicateContinuation<T> {
        return PredicateContinuation { this.or(it.invoke(exp)) }
    }

    infix fun BooleanExpression.and(another: BooleanExpression): BooleanExpression = and(another)
    infix fun BooleanExpression.or(another: BooleanExpression): BooleanExpression = or(another)

}

object JoinerKtOps : ConditionOps, JoinOps

interface JoinOps {

    infix fun JoinDescription.leftJoin(p: EntityPath<*>): JoinDescription {
        return this.nested(J.left(p))
    }

    infix fun JoinDescription.innerJoin(p: EntityPath<*>): JoinDescription {
        return this.nested(J.inner(p))
    }

    infix fun EntityPath<*>.leftJoin(p: EntityPath<*>): JoinDescription {
        return JoinDescription(this).nested(J.left(p))
    }

    infix fun EntityPath<*>.leftJoin(p: JoinDescription): JoinDescription {
        return JoinDescription(this).nested(p.left())
    }

    infix fun EntityPath<*>.innerJoin(p: JoinDescription): JoinDescription {
        return JoinDescription(this).nested(p.inner())
    }

    infix fun EntityPath<*>.innerJoin(p: EntityPath<*>): JoinDescription {
        return JoinDescription(this).nested(J.inner(p))
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.leftJoin(j: JoinDescription): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.joins(j.left())
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.leftJoin(p: EntityPath<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.joins(J.left(p))
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(j: JoinDescription): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.joins(j.inner())
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(p: EntityPath<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.joins(J.inner(p))
        return this
    }

}


class JoinerKtQuery<FROM_C, PROJ, FROM : EntityPath<FROM_C>>(
    private val entityPath: FROM,
    projection: Path<PROJ>,
    isCount: Boolean,
    internal val delegate: JoinerQuery<FROM_C, PROJ> = if (isCount) Q.count(entityPath) as JoinerQuery<FROM_C, PROJ> else Q.select(
        projection
    ).from(entityPath)
//    internal val delegate: JoinerQuery<FROM_C, PROJ> = if (isCount) Q.count(entityPath) as JoinerQuery<FROM_C, PROJ> else Q.select(projection).from(entityPath)
) : JoinerQuery<FROM_C, PROJ> by delegate, JoinOps {

    infix fun where(where: ConditionOps.(e: FROM) -> Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.where(where.invoke(KConditionOps(), entityPath))
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