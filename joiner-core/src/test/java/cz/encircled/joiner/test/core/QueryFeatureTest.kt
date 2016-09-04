package cz.encircled.joiner.test.core

import com.mysema.query.jpa.impl.JPAQuery
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.QueryFeature
import cz.encircled.joiner.test.config.TestConfig
import cz.encircled.joiner.test.model.User
import org.junit.Test
import org.springframework.test.context.ContextConfiguration

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = arrayOf(TestConfig::class))
class QueryFeatureTest : AbstractTest() {

    @Test(expected = TestException::class)
    fun testQueryFeatureBefore() {
        val request = Q.from<User>(QUser.user1)
        request.addFeatures(object : QueryFeature {
            override fun <T> before(request: Q<T>): Q<T> {
                throw TestException()
            }

            override fun after(request: Q<*>, query: JPAQuery): JPAQuery {
                return query
            }
        })
        joiner!!.find(request)
    }

    @Test(expected = TestException::class)
    fun testQueryFeatureAfter() {
        val request = Q.from<User>(QUser.user1)
        request.addFeatures(object : QueryFeature {
            override fun <T> before(request: Q<T>): Q<T> {
                return request
            }

            override fun after(request: Q<*>, query: JPAQuery): JPAQuery {
                throw TestException()
            }
        })
        joiner!!.find(request)
    }

}
