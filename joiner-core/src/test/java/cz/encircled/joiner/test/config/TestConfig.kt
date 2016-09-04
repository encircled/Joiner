package cz.encircled.joiner.test.config

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry
import cz.encircled.joiner.query.join.JoinGraphRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * @author Kisel on 21.01.2016.
 */
@Configuration
@Import(EntityManagerConfig::class)
@ComponentScan(basePackages = arrayOf("cz.encircled.joiner.test.core"))
class TestConfig {

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
