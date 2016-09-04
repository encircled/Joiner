package cz.encircled.joiner.spring

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry
import cz.encircled.joiner.query.join.JoinGraphRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * @author Vlad on 14-Aug-16.
 */
@Configuration
class JoinerConfiguration {

    @PersistenceContext
    private val entityManager: EntityManager? = null

    @Bean
    fun joinGraphRegistry(): JoinGraphRegistry {
        return DefaultJoinGraphRegistry()
    }

    @Bean
    fun joiner(joinGraphRegistry: JoinGraphRegistry): Joiner {
        val joiner = Joiner(entityManager!!)
        joiner.setJoinGraphRegistry(joinGraphRegistry)
        return joiner
    }

}
