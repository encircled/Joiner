package cz.encircled.joiner.kotlin

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtOps.gt
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
import cz.encircled.joiner.model.QUser.user1
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
        assertThrows<JoinerException> { joinerKt.getOne(user1 from user1 where { it.id eq -1 }) }
    }

    @Test
    fun testFindOne() {
        assertNull(joinerKt.findOne(user1 from user1 where { it.id eq -1 }))
    }

    @Test
    fun ktFindAllQueryIntegrationTest() {
        val find = joinerKt.find(
            user1 from user1
                    leftJoin QGroup.group
                    leftJoin QStatus.status
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }

                    asc user1.id
        )

        QAddress.address from QAddress.address where { it.user.id eq (user1.id from user1) }

        assertNotNull(find)
    }

    @Test
    fun `left join on`() {
        val actual = (QGroup.group.all()
                leftJoin user1 on user1.name.eq("user1")).delegate.getJoin(user1).on
        assertEquals(user1.name.eq("user1"), actual)
    }

    @Test
    fun `inner join on`() {
        val actual = (QGroup.group.all()
                innerJoin QGroup.group.users on user1.name.eq("user1")).delegate.getJoin(user1).on
        assertEquals(user1.name.eq("user1"), actual)
    }

    @Test
    fun `tuple with count projection`() {
        val tuples = joinerKt.find(
            listOf(user1.count(), user1.id) from user1
                    groupBy user1.id
        )
        assertEquals(tuples.size, joinerKt.getOne(user1.countOf()).toInt())
    }

    @Test
    fun ktFindOneQueryIntegrationTest() {
        val find = joinerKt.findOne(
            user1 from user1
                    leftJoin QGroup.group
                    leftJoin QStatus.status
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }
                    limit 1
                    offset 0

                    asc user1.id
        )

        assertNotNull(find)
    }

    @Test
    fun ktAppendWhere() {
        assertEquals(user1.id.gt(1).or(user1.id.isNull), (user1.all() orWhere { user1.id.isNull } orWhere { user1.id gt 1 }).where)
        assertEquals(user1.id.gt(1).and(user1.id.isNull), (user1.all() andWhere { user1.id.isNull } andWhere { user1.id gt 1 }).where)
    }

    @Test
    fun ktCountQuery() {
        assertEquals(7, joinerKt.findOne(user1.countOf()))
    }

    @Test
    fun ktCountQueryWithPredicate() {
        assertEquals(1, joinerKt.findOne(user1.countOf() where user1.name.eq("user1")))
    }

    @Test
    fun ktCountQueryIntegrationTest() {
        val find = joinerKt.findOne(
            user1.countOf()
                    leftJoin QGroup.group
                    leftJoin user1.statuses
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }

                    asc user1.id
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