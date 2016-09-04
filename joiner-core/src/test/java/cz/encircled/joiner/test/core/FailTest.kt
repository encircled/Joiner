package cz.encircled.joiner.test.core

import cz.encircled.joiner.exception.AliasMissingException
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import org.junit.Assert
import org.junit.Test

/**
 * @author Kisel on 26.01.2016.
 */
class FailTest : AbstractTest() {

    @Test(expected = IllegalArgumentException::class)
    fun testNullInput() {
        joiner!!.find<Any>(null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullProjection() {
        joiner!!.find<Any, Any>(Q.from<Any>(QAddress.address), null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullQ() {
        joiner!!.find<Any, Any>(null, QUser.user1)
    }

    @Test(expected = AliasMissingException::class)
    fun predicateNoAliasTest() {
        joiner!!.find(Q.from<Any>(QUser.user1).where(QAddress.address.name.eq("user1street1")))
    }

    @Test(expected = JoinerException::class)
    fun testRightJoinFetch() {
        joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QUser.user1).right()))
    }

    @Test(expected = AliasMissingException::class)
    fun testGroupByNoAlias() {
        val avg = joiner!!.find<T, P>(
                Q.from<Any>(QAddress.address).groupBy(QGroup.group.name),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.size > 0)
        Assert.assertTrue(avg.size < joiner!!.find(Q.from<Any>(QAddress.address)).size)
    }

    @Test(expected = AliasMissingException::class)
    fun testGroupByHavingNoAlias() {
        val avg = joiner!!.find<T, P>(
                Q.from<Any>(QAddress.address).groupBy(QAddress.address.user).having(QGroup.group.id.count().gt(2)),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.isEmpty())
    }

}
