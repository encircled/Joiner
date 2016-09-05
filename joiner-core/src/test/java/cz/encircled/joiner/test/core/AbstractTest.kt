package cz.encircled.joiner.test.core

import cz.encircled.joiner.core.Joiner
import cz.encircled.joiner.query.join.JoinGraphRegistry
import cz.encircled.joiner.test.config.TestConfig
import cz.encircled.joiner.test.core.data.TestDataListener
import cz.encircled.joiner.test.model.AbstractEntity
import org.junit.Assert
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.transaction.annotation.Transactional

import javax.persistence.EntityManager
import javax.persistence.Persistence
import javax.persistence.PersistenceContext

/**
 * @author Kisel on 11.01.2016.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(TestConfig::class))
@Transactional
@TestExecutionListeners(listeners = arrayOf(TestDataListener::class))
abstract class AbstractTest : AbstractTransactionalJUnit4SpringContextTests() {

    @Autowired
    protected lateinit var joiner: Joiner

    @Autowired
    protected var joinGraphRegistry: JoinGraphRegistry? = null

    @PersistenceContext
    protected var entityManager: EntityManager? = null

    @Autowired
    private val environment: Environment? = null

    protected fun assertHasName(entities: Collection<AbstractEntity>, name: String) {
        Assert.assertFalse("Found collection must be not empty!", entities.isEmpty())
        for (entity in entities) {
            assertHasName(entity, name)
        }
    }

    protected fun assertHasName(entity: AbstractEntity, name: String) {
        Assert.assertNotNull(entity)
        Assert.assertEquals(name, entity.name)
    }

    protected val isEclipse: Boolean
        get() = hasProfiles("eclipse")

    protected fun hasProfiles(vararg profiles: String): Boolean {
        return environment!!.acceptsProfiles(*profiles)
    }

    protected fun noProfiles(vararg profiles: String): Boolean {
        return !environment!!.acceptsProfiles(*profiles)
    }

    protected fun isLoaded(entity: Any, attribute: String): Boolean {
        return Persistence.getPersistenceUtil().isLoaded(entity, attribute)
    }

}
