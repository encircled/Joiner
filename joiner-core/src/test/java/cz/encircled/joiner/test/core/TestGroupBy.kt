package cz.encircled.joiner.test.core

import cz.encircled.joiner.query.Q
import org.junit.Assert
import org.junit.Test

/**
 * @author Kisel on 28.01.2016.
 */
class TestGroupBy : AbstractTest() {

    @Test
    fun testGroupBy() {
        val avg = joiner!!.find<T, P>(
                Q.from<Any>(QAddress.address).groupBy(QAddress.address.user),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.size > 0)
        Assert.assertTrue(avg.size < joiner!!.find(Q.from<Any>(QAddress.address)).size)
    }

    @Test
    fun testGroupByHaving() {
        val avg = joiner!!.find<T, P>(
                Q.from<Any>(QAddress.address).groupBy(QAddress.address.user).having(QAddress.address.id.count().gt(2)),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.isEmpty())
    }

}
