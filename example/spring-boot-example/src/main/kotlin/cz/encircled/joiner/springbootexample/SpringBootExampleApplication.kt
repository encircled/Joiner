package cz.encircled.joiner.springbootexample

import ch.vorburger.mariadb4j.springboot.autoconfigure.DataSourceAutoConfiguration
import ch.vorburger.mariadb4j.springboot.autoconfigure.MariaDB4jSpringConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(DataSourceAutoConfiguration::class, MariaDB4jSpringConfiguration::class)
class SpringBootExampleApplication {

}

fun main(args: Array<String>) {
    runApplication<SpringBootExampleApplication>(*args)
}
