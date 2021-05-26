package cz.encircled.joiner.springwebfluxexample

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.reactive.ReactorJoiner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import javax.persistence.Persistence

@SpringBootApplication
class SpringWebfluxExampleApplication {

    @Bean
    fun joiner(): ReactorJoiner {
        // Start in-mem DB
        val db = DB.newEmbeddedDB(3306)
        db.start()

        val emf = Persistence.createEntityManagerFactory("reactiveTest")

        return ReactorJoiner(emf)
    }

}

fun main(args: Array<String>) {
    runApplication<SpringWebfluxExampleApplication>(*args)
}
