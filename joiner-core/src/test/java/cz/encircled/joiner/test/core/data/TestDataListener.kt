package cz.encircled.joiner.test.core.data

import org.springframework.test.context.TestContext
import org.springframework.test.context.TestExecutionListener

/**
 * @author Kisel on 26.01.2016.
 */
class TestDataListener : TestExecutionListener {

    @Throws(Exception::class)
    override fun beforeTestClass(testContext: TestContext) {

    }

    @Throws(Exception::class)
    override fun prepareTestInstance(testContext: TestContext) {
        val bean = testContext.applicationContext.getBean(TestData::class.java)
        bean.prepareData()
    }

    @Throws(Exception::class)
    override fun beforeTestMethod(testContext: TestContext) {

    }

    @Throws(Exception::class)
    override fun afterTestMethod(testContext: TestContext) {

    }

    @Throws(Exception::class)
    override fun afterTestClass(testContext: TestContext) {

    }
}
