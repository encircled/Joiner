package cz.encircled.joiner.reactive

import ch.vorburger.mariadb4j.DB
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

private lateinit var db: DB
private lateinit var emf: EntityManagerFactory

open class WithInMemMySql {

    lateinit var reactorJoiner: ReactorJoiner

    @Before
    fun beforeEach() {
        if (!this::reactorJoiner.isInitialized) {
            reactorJoiner = ReactorJoiner(emf)
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun before() {
            db = DB.newEmbeddedDB(3306)
            db.start()

            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        @AfterClass
        @JvmStatic
        fun after() {
            db.stop()
        }
    }

}