package cz.encircled.joiner.springbootexample

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.kotlin.JoinerKt
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JoinerConfig {

    @PersistenceContext
    lateinit var em: EntityManager

    @Bean
    fun joiner(): Joiner {
        return Joiner(em)
    }

    @Bean
    fun joinerKt(): JoinerKt {
        return JoinerKt(em)
    }

}