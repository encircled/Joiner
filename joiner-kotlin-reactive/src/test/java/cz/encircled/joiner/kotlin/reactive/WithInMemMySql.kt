package cz.encircled.joiner.kotlin.reactive

import ch.vorburger.mariadb4j.DB
import cz.encircled.joiner.TestWithLogging
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.model.QUser
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInfo
import org.slf4j.LoggerFactory
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence
import kotlin.test.BeforeTest


open class WithInMemMySql : TestWithLogging() {

    lateinit var joiner: KtReactiveJoiner

    companion object {
        @JvmStatic
        private var db: DB? = null

        @JvmStatic
        private var emf: EntityManagerFactory? = null

        @JvmStatic
        @BeforeAll
        fun before() {
            if (db == null) {
                LoggerFactory.getLogger(this::class.java).info("Starting DB on 3306 port")
                db = DB.newEmbeddedDB(3306)
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

    @BeforeTest
    override fun beforeEach(testInfo: TestInfo) {
        super.beforeEach(testInfo)

        if (!this::joiner.isInitialized) {
            joiner = KtReactiveJoiner(emf!!)
        }

        runBlocking {
            joiner.find(QUser.user1.all()).forEach { joiner.remove(it) }
        }
    }

}