package cz.encircled.joiner.test.core

import cz.encircled.joiner.exception.AliasMissingException
import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.QAddress
import cz.encircled.joiner.test.model.QGroup
import cz.encircled.joiner.test.model.QUser
import org.junit.Assert
import org.junit.Test

/**
 * @author Kisel on 26.01.2016.
 */
class FailTest : AbstractTest() {

    @Test(expected = AliasMissingException::class)
    fun predicateNoAliasTest() {
        joiner.find(Q.from(QUser.user1).where(QAddress.address.name.eq("user1street1")))
    }

    @Test(expected = JoinerException::class)
    fun testRightJoinFetch() {
        joiner.find(Q.from(QGroup.group).joins(J.left(QUser.user1).right()))
    }

    @Test(expected = AliasMissingException::class)
    fun testGroupByNoAlias() {
        val avg = joiner.find<T, P>(
                Q.from(QAddress.address).groupBy(QGroup.group.name),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.size > 0)
        Assert.assertTrue(avg.size < joiner.find(Q.from(QAddress.address)).size)
    }

    @Test(expected = AliasMissingException::class)
    fun testGroupByHavingNoAlias() {
        val avg = joiner.find<T, P>(
                Q.from(QAddress.address).groupBy(QAddress.address.user).having(QGroup.group.id.count().gt(2)),
                QAddress.address.id.avg())
        Assert.assertTrue(avg.isEmpty())
    }

}
