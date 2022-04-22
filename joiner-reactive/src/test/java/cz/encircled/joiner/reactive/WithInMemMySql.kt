package cz.encircled.joiner.reactive

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.TestWithLogging
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import reactor.test.StepVerifier
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import kotlin.test.BeforeTest

//private var emf: EntityManagerFactory? = null

abstract class WithInMemMySql : TestWithLogging() {

    companion object {
        @JvmStatic
        private var db: DB? = null
        @JvmStatic
        private var emf: EntityManagerFactory? = null
    }

    lateinit var reactorJoiner: ReactorJoiner

    @BeforeTest
    fun beforeEach() {
        if (db == null) {
            log.info("Starting DB on 3307 port")
            db = DB.newEmbeddedDB(3307)
            db!!.start()
        }
        if (emf == null) {
            log.info("createEntityManagerFactory(\"reactiveTest\")")
            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        reactorJoiner = ReactorJoiner(emf!!)
/*
        if (db == null) {
            log.info("Starting DB on 3307 port")
            db = DB.newEmbeddedDB(3307)
            db!!.start()
        }
        if (emf == null) {
            log.info("createEntityManagerFactory(\"reactiveTest\")")
            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        if (!this::reactorJoiner.isInitialized) {
            reactorJoiner = ReactorJoiner(emf!!)
        }
*/

        log.info("Drop old test data")
        StepVerifier.create(reactorJoiner.find(QUser.user1.all())
            .flatMap { reactorJoiner.remove(it) }
            .collectList()
        )
            .expectNextMatches { true }
            .verifyComplete()
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