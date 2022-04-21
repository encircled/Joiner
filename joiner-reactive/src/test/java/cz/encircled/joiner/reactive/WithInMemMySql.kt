package cz.encircled.joiner.reactive

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
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

        reactorJoiner.find(QUser.user1.all())
            .flatMap { reactorJoiner.remove(it) }
            .collectList()
            .block()
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            db = DB.newEmbeddedDB(3307)
            db.start()

            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        @AfterAll
        @JvmStatic
        fun after() {
            db.stop()
        }
    }

    fun Throwable.hasCause(msg: String) =
        this is JoinerException && message!!.contains(msg) ||
                cause is JoinerException && cause!!.message!!.contains(msg)

}