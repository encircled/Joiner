package cz.encircled.joiner.kotlin

import cz.encircled.joiner.core.Joiner
import javax.persistence.EntityManager

class JoinerKt(entityManager: EntityManager) : Joiner(entityManager), JoinOps