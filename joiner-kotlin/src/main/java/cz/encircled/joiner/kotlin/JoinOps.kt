package cz.encircled.joiner.kotlin

import com.querydsl.core.types.EntityPath
import com.querydsl.core.types.dsl.CollectionPathBase
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.query.join.JoinDescription

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

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.leftJoin(p: CollectionPathBase<*, *, *>): JoinerKtQuery<FROM_C, PROJ, FROM> {
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

    infix fun <FROM_C, PROJ, FROM : EntityPath<FROM_C>> JoinerKtQuery<FROM_C, PROJ, FROM>.innerJoin(p: CollectionPathBase<*, *, *>): JoinerKtQuery<FROM_C, PROJ, FROM> {
        delegate.joins(J.inner(p))
        return this
    }

}
