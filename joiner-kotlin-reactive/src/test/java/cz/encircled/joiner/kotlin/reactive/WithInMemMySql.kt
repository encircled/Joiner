package cz.encircled.joiner.kotlin.reactive

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import kotlin.test.BeforeTest

private var db: DB? = null
private lateinit var emf: EntityManagerFactory

open class WithInMemMySql {

    lateinit var joiner: KtReactiveJoiner

    @BeforeTest
    fun beforeEach() {
        if (db == null) {
            db = DB.newEmbeddedDB(3306)
            db!!.start()

            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }
        if (!this::joiner.isInitialized) {
            joiner = KtReactiveJoiner(emf)
        }

        runBlocking {
            joiner.find(QUser.user1.all()).forEach { joiner.remove(it) }
        }
    }

    companion object {
        @AfterAll
        @JvmStatic
        fun after() {
            //db.stop()
        }
    }

    fun Throwable.hasCause(msg: String) =
        this is JoinerException && message!!.contains(msg) ||
                cause is JoinerException && cause!!.message!!.contains(msg)

}