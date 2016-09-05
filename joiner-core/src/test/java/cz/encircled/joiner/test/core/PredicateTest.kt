package cz.encircled.joiner.test.core

import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import org.junit.Test

/**
 * @author Kisel on 26.01.2016.
 */
class PredicateTest : AbstractTest() {

    @Test
    fun basicPredicateTest() {
        val name = "user1"
        val result = joiner.find(Q.from(QUser.user1).where(QUser.user1.name.eq(name)))
        assertHasName(result, name)
    }

    @Test
    fun predicateInCollectionTest() {
        val name = "user1street1"
        val result = joiner.find(Q.from(QUser.user1).joins(J.left(QAddress.address)).where(QAddress.address.name.eq(name)))
        assertHasName(result, "user1")
        assertHasName(result[0].addresses, name)
    }

    @Test
    fun predicateInSinglePathTest() {
        val name = "user1"
        val result = joiner.find(Q.from(QAddress.address).joins(J.left(QUser.user1)).where(QAddress.address.user.name.eq(name)))

        for (address in result) {
            assertHasName(address.user, name)
        }
    }

}
