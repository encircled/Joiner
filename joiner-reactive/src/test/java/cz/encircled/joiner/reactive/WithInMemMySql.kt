package cz.encircled.joiner.reactive

import ch.vorburger.mariadb4j.DB
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import kotlin.test.BeforeTest

private lateinit var db: DB
private lateinit var emf: EntityManagerFactory

open class WithInMemMySql {

    lateinit var reactorJoiner: ReactorJoiner

    @BeforeTest
    fun beforeEach() {
        if (!this::reactorJoiner.isInitialized) {
            reactorJoiner = ReactorJoiner(emf)
        }
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            db = DB.newEmbeddedDB(3306)
            db.start()

            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        @AfterAll
        @JvmStatic
        fun after() {
            db.stop()
        }
    }

}