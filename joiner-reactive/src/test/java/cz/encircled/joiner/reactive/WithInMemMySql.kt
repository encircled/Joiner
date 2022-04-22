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

private var db: DB? = null
private lateinit var emf: EntityManagerFactory

open class WithInMemMySql : TestWithLogging() {

    lateinit var reactorJoiner: ReactorJoiner

    @BeforeTest
    fun beforeEach() {
        if (db == null) {
            db = DB.newEmbeddedDB(3307)
            db!!.start()

            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }

        if (!this::reactorJoiner.isInitialized) {
            reactorJoiner = ReactorJoiner(emf)
        }

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