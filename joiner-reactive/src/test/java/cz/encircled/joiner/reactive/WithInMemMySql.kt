package cz.encircled.joiner.reactive

import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import cz.encircled.joiner.TestWithLogging
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.Persistence
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.slf4j.LoggerFactory
import reactor.test.StepVerifier
import kotlin.test.BeforeTest

abstract class WithInMemMySql : TestWithLogging() {

    companion object {
        @JvmStatic
        private var db: DB? = null

        @JvmStatic
        private var emf: EntityManagerFactory? = null

        @JvmStatic
        @BeforeAll
        fun before() {
            if (db == null) {
                LoggerFactory.getLogger(this::class.java).info("Starting DB on 3307 port")
                val build = DBConfigurationBuilder.newBuilder()
//                    .setDatabaseVersion("mariaDB4j-db-10.3")
                    .setPort(3307)
                    .build()
                db = DB.newEmbeddedDB(build)
                db!!.start()
            }
            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        @JvmStatic
        @AfterAll
        fun after() {
            try {
                emf?.close()
                db?.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            db = null
            emf = null
        }

    }

    lateinit var reactorJoiner: ReactorJoiner

    @BeforeTest
    fun beforeEach() {
        reactorJoiner = ReactorJoiner(emf!!)

        log.info("Drop old test data")
        val users = reactorJoiner.transaction {
            find(QUser.user1 from QUser.user1)
        }.collectList().block()
        users?.forEach {
            log.info("Remove user " + it.name)
            reactorJoiner.remove(it).block()
        }
        log.info("Drop old test data finished")
    }

    fun createUsers(vararg names: String = arrayOf("1", "2")) {
        StepVerifier.create(reactorJoiner.persist(names.map { User(it) }).map { it.name }.collectList())
            .expectNext(names.toList())
            .verifyComplete()
    }

    fun Throwable.hasCause(msg: String) =
        this is JoinerException && message!!.contains(msg) ||
                cause is JoinerException && cause!!.message!!.contains(msg)

}