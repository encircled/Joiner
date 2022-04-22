package cz.encircled.joiner.kotlin.reactive

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.TestWithLogging
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import kotlin.test.BeforeTest

private var db: DB? = null
private lateinit var emf: EntityManagerFactory

open class WithInMemMySql : TestWithLogging() {

    lateinit var joiner: KtReactiveJoiner

    @BeforeTest
    override fun beforeEach(testInfo : TestInfo) {
        super.beforeEach(testInfo)
        if (db == null) {
            log.info("Starting DB on 3306 port")
            db = DB.newEmbeddedDB(3306)
            db!!.start()

            log.info("createEntityManagerFactory(\"reactiveTest\")")
            emf = Persistence.createEntityManagerFactory("reactiveTest")
        }
        if (!this::joiner.isInitialized) {
            joiner = KtReactiveJoiner(emf)
        }

        runBlocking {
            joiner.find(QUser.user1.all()).forEach { joiner.remove(it) }
        }
    }

}