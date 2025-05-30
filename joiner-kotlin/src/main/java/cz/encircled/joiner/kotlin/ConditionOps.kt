
package cz.encircled.joiner.kotlin

import com.querydsl.core.types.Expression
import com.querydsl.core.types.SubQueryExpression
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.core.types.dsl.NumberExpression
import com.querydsl.core.types.dsl.SimpleExpression
import com.querydsl.core.types.dsl.StringExpression

interface ConditionOps {

    /**
     * Equals to ignoring case
     */
    infix fun StringExpression.eqic(to: String): BooleanExpression = equalsIgnoreCase(to)

    infix fun StringExpression.contains(to: String): BooleanExpression = contains(to)

    infix fun <T> SimpleExpression<T>.eq(to: T): BooleanExpression = eq(to)

    infix fun <T> SimpleExpression<T>.eq(to: Expression<in T>): BooleanExpression = eq(to)

    infix fun <T> SimpleExpression<T>.isIn(to: Collection<out T>): BooleanExpression = `in`(to)

    infix fun <T> SimpleExpression<T>.isIn(to: SubQueryExpression<T>): BooleanExpression = `in`(to)

    infix fun <T> SimpleExpression<T>.notIn(to: Collection<T>): BooleanExpression = notIn(to)

    infix fun <T> SimpleExpression<T>.notIn(to: SubQueryExpression<T>): BooleanExpression = notIn(to)

    /**
     * Not equals
     */
    infix fun <T> SimpleExpression<T>.ne(to: T): BooleanExpression = ne(to)

    infix fun <T> PredicateContinuation<T>.eq(to: T): BooleanExpression {
        return chain.invoke { it.eq(to) }
    }

    infix fun <T> PredicateContinuation<T>.eq(to: Expression<in T>): BooleanExpression {
        return chain.invoke { it.eq(to) }
    }

    /**
     * Not equals
     */
    infix fun <T> PredicateContinuation<T>.ne(to: T): BooleanExpression {
        return chain.invoke { it.ne(to) }
    }

    infix fun <T> PredicateContinuation<T>.isIn(to: Collection<T>): BooleanExpression {
        return chain.invoke { it.`in`(to) }
    }

    infix fun <T> PredicateContinuation<T>.isIn(to: SubQueryExpression<T>): BooleanExpression {
        return chain.invoke { it.`in`(to) }
    }

    infix fun <T> PredicateContinuation<T>.notIn(to: Collection<T>): BooleanExpression {
        return chain.invoke { it.notIn(to) }
    }

    infix fun <T> PredicateContinuation<T>.notIn(to: SubQueryExpression<T>): BooleanExpression {
        return chain.invoke { it.notIn(to) }
    }

    infix fun <T> BooleanExpression.and(exp: SimpleExpression<T>): PredicateContinuation<T> {
        return PredicateContinuation { this.and(it.invoke(exp)) }
    }

    infix fun <T> BooleanExpression.or(exp: SimpleExpression<T>): PredicateContinuation<T> {
        return PredicateContinuation { this.or(it.invoke(exp)) }
    }

    infix fun BooleanExpression.and(another: BooleanExpression): BooleanExpression = and(another)

    infix fun BooleanExpression.or(another: BooleanExpression): BooleanExpression = or(another)

    // LIKES

    infix fun StringExpression.startsWith(to: String): BooleanExpression = startsWith(to)

    infix fun StringExpression.startsWith(to: Expression<String>): BooleanExpression = startsWith(to)

    infix fun StringExpression.startsWithIc(to: String): BooleanExpression = startsWithIgnoreCase(to)

    infix fun StringExpression.startsWithIc(to: Expression<String>): BooleanExpression = startsWithIgnoreCase(to)

    infix fun StringExpression.like(to: String): BooleanExpression = like(to)

    infix fun StringExpression.like(to: Expression<String>): BooleanExpression = like(to)

    /**
     * Like ignore case
     */
    infix fun StringExpression.likeic(to: String): BooleanExpression = likeIgnoreCase(to)

    /**
     * Like ignore case
     */
    infix fun StringExpression.likeic(to: Expression<String>): BooleanExpression = likeIgnoreCase(to)

    // NUMBERS

    /**
     * Greater than
     */
    infix fun <T> NumberExpression<T>.gt(to: T): BooleanExpression where T : Number, T : Comparable<*> = gt(to)

    infix fun <T> NumberExpression<*>.goe(to: T): BooleanExpression where T : Number, T : Comparable<*> = goe(to)

    /**
     * Less than
     */
    infix fun <T> NumberExpression<T>.lt(to: T): BooleanExpression where T : Number, T : Comparable<*> = lt(to)

    infix fun <T> NumberExpression<*>.loe(to: T): BooleanExpression where T : Number, T : Comparable<*> = loe(to)

}