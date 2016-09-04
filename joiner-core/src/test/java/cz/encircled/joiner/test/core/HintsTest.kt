package cz.encircled.joiner.test.core

import cz.encircled.joiner.query.Q
import cz.encircled.joiner.test.config.TestConfig
import cz.encircled.joiner.test.config.hint.HintQueryFeature
import cz.encircled.joiner.test.model.User
import org.junit.Test
import org.springframework.test.context.ContextConfiguration

/**
 * @author Kisel on 04.02.2016.
 */
@ContextConfiguration(classes = arrayOf(TestConfig::class))
class HintsTest : AbstractTest() {

    @Test
    fun testHint() {
        joiner!!.find(Q.from<Any>(QUser.user1).addHint("testHint", "testHintValue").addFeatures(HintQueryFeature()))
    }

    @Test(expected = TestException::class)
    fun testOfTest() {
        joiner!!.find(Q.from<Any>(QUser.user1).addHint("testHint", "exception").addFeatures(HintQueryFeature()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullKeyHint() {
        joiner!!.find(Q<User>().addHint(null, "testHintValue").addFeatures(HintQueryFeature()))
    }

}
