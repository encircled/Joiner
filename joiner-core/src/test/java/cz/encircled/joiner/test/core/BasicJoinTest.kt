package cz.encircled.joiner.test.core

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.Address
import cz.encircled.joiner.test.model.Group
import cz.encircled.joiner.test.model.SuperUser
import cz.encircled.joiner.test.model.User
import org.junit.Assert
import org.junit.Test
import javax.persistence.Persistence

/**
 * @author Kisel on 21.01.2016.
 */
class BasicJoinTest : AbstractTest() {

    @Test
    fun noFetchJoinTest() {
        var users = joiner!!.find(Q.from<User>(QUser.user1))
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users[0], "groups"))

        val e = J.left(QGroup.group).fetch(false)

        users = joiner!!.find(Q.from<Any>(QUser.user1).joins(listOf(e)))
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users[0], "groups"))

        e.fetch(true)
        entityManager!!.clear()
        users = joiner!!.find(Q.from<Any>(QUser.user1).joins(listOf(e)))
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(users[0], "groups"))
    }

    @Test
    fun testFoundSubclassPredicated() {
        val groups = joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QSuperUser.superUser).nested(J.left(QKey.key))).joins(J.left(QStatus.status)).where(QSuperUser.superUser.key.name.eq("key1")))

        Assert.assertFalse(groups.isEmpty())

        for (group in groups) {
            var hasKey = false
            for (user in group.users) {
                if (user is SuperUser) {
                    if (user.key.name == "key1") {
                        hasKey = true
                    }
                }
            }
            Assert.assertTrue(hasKey)
        }
    }

    @Test
    fun testNotFoundSubclassPredicated() {
        val groups = joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QSuperUser.superUser).nested(J.left(QKey.key)),
                J.left(QStatus.status)).where(J.path<EntityPath>(QSuperUser.superUser, QKey.key).name.eq("not_exists")))

        Assert.assertTrue(groups.isEmpty())
    }

    @Test
    fun testNestedCollectionAndSingleJoin() {
        if (noProfiles("eclipse")) {
            val addresses = joiner!!.find(Q.from<Address>(QAddress.address))
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses[0], "user"))
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses[0].user, "groups"))
            entityManager!!.clear()
        }

        val addresses = joiner!!.find(Q.from<Any>(QAddress.address).joins(J.left(QUser.user1).nested(J.left(QGroup.group))))

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses[0], "user"))
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses[0].user, "groups"))
    }


    @Test
    fun testNestedCollectionJoin() {
        var groups = joiner!!.find(Q.from<Group>(QGroup.group))

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups[0], "users"))
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups[0].users.iterator().next(), "addresses"))

        entityManager!!.clear()

        groups = joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QUser.user1).nested(J.left(QAddress.address))))

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups[0], "users"))
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups[0].users.iterator().next(), "addresses"))
    }

    @Test
    fun testInnerJoin() {
        val q = Q.from<Any>(QUser.user1).joins(J.inner(QAddress.address))

        Assert.assertFalse(joiner!!.find<User>(q).isEmpty())

        q.where(QUser.user1.name.eq("user3"))
        Assert.assertTrue(joiner!!.find<User>(q).isEmpty())
    }

    @Test
    fun nonCollisionAliasCollectionJoinTest() {
        joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QStatus.status)))
    }

    @Test
    fun testRightJoinNoFetch() {
        if (noProfiles("eclipse")) {
            val groups = joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QUser.user1).right().fetch(false)).where(QUser.user1.name.eq("user1")))
            Assert.assertFalse(groups.isEmpty())
        }
    }

    @Test(expected = JoinerException::class)
    fun testRightJoinNoFetchEclipse() {
        if (isEclipse) {
            val groups = joiner!!.find(Q.from<Any>(QGroup.group).joins(J.left(QUser.user1).right().fetch(false)).where(QUser.user1.name.eq("user1")))
            Assert.assertFalse(groups.isEmpty())
        } else {
            throw JoinerException("Test")
        }
    }

    @Test
    fun testNonDistinct() {
        val nonDistinct = joiner!!.find(Q.from<Any>(QUser.user1).joins(J.left(QAddress.address).nested(J.left(QStatus.status))).distinct(false)).size

        entityManager!!.clear()

        val distinct = joiner!!.find(Q.from<Any>(QUser.user1).joins(J.left(QAddress.address))).size

        if (isEclipse) {
            Assert.assertTrue(distinct == nonDistinct)
        } else {
            Assert.assertTrue(distinct < nonDistinct)
        }
    }

    @Test
    fun testJoinOn() {
        val name = "user1"

        val groups = joiner!!.find<Any, User>(Q.from<Any>(QGroup.group).joins(J.inner(QUser.user1).on(QUser.user1.name.eq(name)).fetch(false)), QUser.user1)
        assertHasName(groups, name)
    }

}
