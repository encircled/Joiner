package cz.encircled.joiner.springwebfluxexample

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.kotlin.reactive.KtReactiveJoiner
import cz.encircled.joiner.reactive.ReactorJoiner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence

@SpringBootApplication
class SpringWebfluxExampleApplication {

    @Bean
    fun emf(): EntityManagerFactory {
        val db = DB.newEmbeddedDB(3306)
        db.start()
        val createEntityManagerFactory = Persistence.createEntityManagerFactory("reactiveTest")
        return createEntityManagerFactory
    }

    @Bean
    fun reactorJoiner(emf : EntityManagerFactory): ReactorJoiner {
        return ReactorJoiner(emf)
    }

    @Bean
    fun kotlinReactiveJoiner(emf : EntityManagerFactory): KtReactiveJoiner {
        return KtReactiveJoiner(emf)
    }

}

fun main(args: Array<String>) {
    runApplication<SpringWebfluxExampleApplication>(*args)
}
