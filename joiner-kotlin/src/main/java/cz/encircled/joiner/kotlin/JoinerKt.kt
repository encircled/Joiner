package cz.encircled.joiner.kotlin

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.join.JoinDescription
import javax.persistence.EntityManager

class JoinerKt(entityManager: EntityManager) : Joiner(entityManager), JoinOps {
    override var lastJoin: JoinDescription? = null
}