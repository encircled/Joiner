package cz.encircled.joiner.test.core

import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.Group
import cz.encircled.joiner.test.model.NormalUser
import cz.encircled.joiner.test.model.SuperUser
import org.junit.Assert
import org.junit.Assume
import org.junit.Before
import org.junit.Test
import javax.persistence.Persistence

/**
 * Created by Kisel on 28.01.2016.
 */
class InheritanceJoinTest : AbstractTest() {

    @Before
    fun before() {
        Assume.assumeTrue(noProfiles("eclipse"))
    }

    @Test
    fun joinSingleEntityOnChildTest() {
        val groups = joiner.find(Q.from(QGroup.group).joins(J.left(QUser.user1).alias(QUser("superUser")), J.left(QSuperUser.superUser.key)))

        check(groups, true, false)
    }

    @Test
    fun joinSingleAndCollectionMultipleChildrenTest() {
        val groups = joiner.find(Q<Group>().joins(J.left(QUser.user1).nested(J.left(QKey.key), J.left(QPassword.password))).where(QKey.key.name.ne("bad_key")))

        check(groups, true, false)
    }

    @Test
    fun joinCollectionOnChildTest() {
        val groups = joiner.find(Q.from(QGroup.group).joins(J.left(QUser.user1).alias(QNormalUser.normalUser._super).nested(J.left(QPassword.password))))

        check(groups, false, true)
    }

    @Test
    fun nestedTest() {
        val addresses = joiner.find(Q.from(QAddress.address).joins(J.left(QUser.user1).nested(J.left(QPassword.password))))

        Assert.assertFalse(addresses.isEmpty())
        for (address in addresses) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(address, "user"))
            Assert.assertTrue(address.user is NormalUser)

            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(address.user, "passwords"))
        }
    }

    private fun check(groups: List<Group>, key: Boolean, password: Boolean) {
        var hasKey = false
        var hasPassword = false

        for (group in groups) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"))
            for (user in group.users) {
                if (user is SuperUser) {
                    if (key) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "key"))
                    }
                    hasKey = true
                }
                if (user is NormalUser) {
                    if (password) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"))
                    }
                    hasPassword = true
                }
            }
        }

        if (key) {
            Assert.assertTrue(hasKey)
        }
        if (password) {
            Assert.assertTrue(hasPassword)
        }
    }

}
