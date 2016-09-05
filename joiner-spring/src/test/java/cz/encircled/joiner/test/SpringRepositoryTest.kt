package cz.encircled.joiner.test

import cz.encircled.joiner.query.Q
import cz.encircled.joiner.test.config.SpringTestConfig
import cz.encircled.joiner.test.model.User
import cz.encircled.joiner.test.repository.UserRepository
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * @author Vlad on 14-Aug-16.
 */
@RunWith(SpringJUnit4ClassRunner::class)
@ContextConfiguration(classes = arrayOf(SpringTestConfig::class))
@Transactional
class SpringRepositoryTest {

    @Autowired
    private val userRepository: UserRepository? = null

    @PersistenceContext
    private val entityManager: EntityManager? = null

    @Test
    fun testSpringRepository() {
        val user = User()
        val testName = "testName"
        user.name = testName

        entityManager!!.persist(user)
        entityManager.flush()
        entityManager.clear()

        val found = userRepository!!.findOne(Q.from(QUser.user1).where(QUser.user1.name.eq(testName)))
        Assert.assertNotNull(found)
        Assert.assertEquals(testName, found.name)
    }

}
