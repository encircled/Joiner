package cz.encircled.joiner.kotlin.reactive

import cz.encircled.joiner.reactive.GenericHibernateReactiveJoiner
import javax.persistence.EntityManagerFactory

class KtReactiveJoiner(emf: EntityManagerFactory) : GenericHibernateReactiveJoiner(emf) {


}