package cz.encircled.joiner.kotlin

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.CollectionPathBase
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription

interface JoinOps {

    var lastJoin: JoinDescription?

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
        val join = j.left()
        lastJoin = j
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.leftJoin(p: EntityPath<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = J.left(p)
        lastJoin = join
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.leftJoin(p: CollectionPathBase<*, *, *>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = J.left(p)
        lastJoin = join
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(j: JoinDescription): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = j.inner()
        lastJoin = j
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(p: EntityPath<*>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = J.inner(p)
        lastJoin = join
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(p: CollectionPathBase<*, *, *>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = J.inner(p)
        lastJoin = join
        delegate.joins(join)
        return this
    }

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.on(p: Predicate): JoinerKtQuery<FROM_C, PROJ, FROM> {
        val join = lastJoin ?: throw IllegalStateException("Add a join statement before 'on' condition")
        join.on(p)
        return this
    }

}
