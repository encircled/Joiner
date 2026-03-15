package cz.encircled.joiner.kotlin

import com.querydsl.core.types.EntityPath
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

}
