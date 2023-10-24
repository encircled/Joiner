package cz.encircled.joiner.kotlin

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.exception.JoinerExceptions
import cz.encircled.joiner.query.JoinerQuery
import cz.encircled.joiner.query.join.JoinDescription
import jakarta.persistence.EntityManager

class JoinerKt(entityManager: EntityManager) : Joiner(entityManager), JoinOps {
    override var lastJoin: JoinDescription? = null

    override fun <T : Any?, R : Any?> findOne(request: JoinerQuery<T, R>?): R? {
        return super.findOne(request)
    }

    fun <T : Any?, R : Any?> getOne(request: JoinerQuery<T, R>?): R {
        return super.findOne(request) ?: throw JoinerExceptions.entityNotFound()
    }

}
