package cz.encircled.joiner.test.core

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.Group
import cz.encircled.joiner.test.model.User
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.springframework.transaction.annotation.Transactional

/**
 * @author Vlad on 15-Aug-16.
 */
@Transactional
class JoinGraphTest : AbstractTest() {

    private var mockRegistry: DefaultJoinGraphRegistry? = null

    @Before
    fun before() {
        mockRegistry = DefaultJoinGraphRegistry()
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullName() {
        mockRegistry!!.registerJoinGraph(null, listOf(J.left(QUser.user1)), Group::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullJoins() {
        mockRegistry!!.registerJoinGraph("test", null, Group::class.java)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testNullClass() {
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)), null)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testEmptyClasses() {
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)))
    }

    @Test(expected = JoinerException::class)
    fun testDuplicatedName() {
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)), Group::class.java)
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)), Group::class.java)
    }

    @Test
    fun testDuplicatedNameForDifferentClasses() {
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)), Group::class.java)
        mockRegistry!!.registerJoinGraph("test", listOf(J.left(QUser.user1)), User::class.java)
    }

    @Test
    fun testAddToRegistry() {
        val joins = listOf(J.left(QUser.user1))
        val joins2 = listOf(J.left(QStatus.status))

        mockRegistry!!.registerJoinGraph("users", joins, Group::class.java)
        mockRegistry!!.registerJoinGraph("statuses", joins2, Group::class.java)

        Assert.assertEquals(joins, mockRegistry!!.getJoinGraph(Group::class.java, "users"))
        Assert.assertEquals(joins2, mockRegistry!!.getJoinGraph(Group::class.java, "statuses"))
    }

    @Test
    fun testQueryWithSingleJoinGraph() {
        joinGraphRegistry!!.registerJoinGraph("fullUsers", listOf(J.left(QUser.user1).nested(J.left(QStatus.status))), Group::class.java)

        val groups = joiner.find(Q.from(QGroup.group).joinGraphs("fullUsers"))

        assertUserAndStatusesFetched(groups, false)
    }

    @Test
    fun testQueryWithMultipleJoinGraph() {
        joinGraphRegistry!!.registerJoinGraph("statuses", listOf(J.left(QStatus.status)), Group::class.java)
        joinGraphRegistry!!.registerJoinGraph("users", listOf(J.left(QUser.user1)), Group::class.java)
        joinGraphRegistry!!.registerJoinGraph("userStatuses", listOf(J.left(QUser.user1).nested(J.left(QStatus.status))), Group::class.java)

        val groups = joiner.find(Q.from(QGroup.group).joinGraphs("statuses", "users", "userStatuses"))

        assertUserAndStatusesFetched(groups, true)
    }

    private fun assertUserAndStatusesFetched(groups: List<Group>, isGroupStatusFetched: Boolean) {
        Assert.assertFalse(groups.isEmpty())

        for (group in groups) {
            Assert.assertTrue(isLoaded(group, "users"))
            Assert.assertEquals(isGroupStatusFetched, isLoaded(group, "statuses"))
            for (user in group.users) {
                Assert.assertTrue(isLoaded(user, "statuses"))
            }
        }
    }

}
