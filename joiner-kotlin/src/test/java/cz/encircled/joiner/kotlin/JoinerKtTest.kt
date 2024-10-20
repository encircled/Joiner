package cz.encircled.joiner.kotlin

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.isIn
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.ne
import cz.encircled.joiner.kotlin.JoinerKtOps.notIn
import cz.encircled.joiner.kotlin.JoinerKtOps.on
import cz.encircled.joiner.kotlin.JoinerKtOps.or
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QAddress
import cz.encircled.joiner.model.QGroup
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class JoinerKtTest : AbstractTest() {

    lateinit var joinerKt: JoinerKt

    @BeforeTest
    fun before(test: TestInfo) {
        super.beforeEach(test)
        joinerKt = JoinerKt(entityManager)
    }

    @Test
    fun getOne() {
        assertThrows<JoinerException> { joinerKt.getOne(QUser.user1 from QUser.user1 where { it.id eq -1 }) }
    }

    @Test
    fun testFindOne() {
        assertNull(joinerKt.findOne(QUser.user1 from QUser.user1 where { it.id eq -1 }))
    }

    @Test
    fun ktFindAllQueryIntegrationTest() {
        val find = joinerKt.find(
            QUser.user1 from QUser.user1
                    leftJoin QGroup.group
                    leftJoin QStatus.status
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }

                    asc QUser.user1.id
        )

        QAddress.address from QAddress.address where { it.user.id eq (QUser.user1.id from QUser.user1) }

        assertNotNull(find)
    }

    @Test
    fun `left join on`() {
        val actual = (QGroup.group.all()
                leftJoin QUser.user1 on QUser.user1.name.eq("user1")).delegate.getJoin(QUser.user1).on
        assertEquals(QUser.user1.name.eq("user1"), actual)
    }

    @Test
    fun `inner join on`() {
        val actual = (QGroup.group.all()
                innerJoin QGroup.group.users on QUser.user1.name.eq("user1")).delegate.getJoin(QUser.user1).on
        assertEquals(QUser.user1.name.eq("user1"), actual)
    }

    @Test
    fun `tuple with count projection`() {
        val tuples = joinerKt.find(
            listOf(QUser.user1.count(), QUser.user1.id) from QUser.user1
                    groupBy QUser.user1.id
        )
        assertEquals(tuples.size, joinerKt.getOne(QUser.user1.countOf()).toInt())
    }

    @Test
    fun ktFindOneQueryIntegrationTest() {
        val find = joinerKt.findOne(
            QUser.user1 from QUser.user1
                    leftJoin QGroup.group
                    leftJoin QStatus.status
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }
                    limit 1
                    offset 0

                    asc QUser.user1.id
        )

        assertNotNull(find)
    }

    @Test
    fun ktCountQuery() {
        assertEquals(7, joinerKt.findOne(QUser.user1.countOf()))
    }

    @Test
    fun ktCountQueryWithPredicate() {
        assertEquals(1, joinerKt.findOne(QUser.user1.countOf() where QUser.user1.name.eq("user1")))
    }

    @Test
    fun ktCountQueryIntegrationTest() {
        val find = joinerKt.findOne(
            QUser.user1.countOf()
                    leftJoin QGroup.group
                    leftJoin QUser.user1.statuses
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }

                    asc QUser.user1.id
        )

        assertEquals(7, find)
    }

    @Test
    fun `subquery predicate - in op`() {
        val result = joinerKt.find(QGroup.group.all() where { it.id isIn (QGroup.group.id from QGroup.group limit 1) })
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `subquery predicate - not in op`() {
        val result = joinerKt.find(QGroup.group.all() where { it.id notIn (QGroup.group.id from QGroup.group) })
        assertTrue(result.isEmpty())
    }

    @Test
    fun `subquery predicate - eq op`() {
        val result = joinerKt.find(QGroup.group.all() where { it.id eq (QGroup.group.id from QGroup.group limit 1) })
        assertTrue(result.isNotEmpty())
    }


}