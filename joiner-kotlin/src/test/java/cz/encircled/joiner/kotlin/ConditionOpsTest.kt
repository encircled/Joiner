package cz.encircled.joiner.kotlin

import cz.encircled.joiner.model.QUser.user1
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionOpsTest : ConditionOps {

    @Test
    fun testInfixConditions() {
        assertEquals(user1.name.contains("1"), user1.name contains "1")
        assertEquals(user1.name.ne("1"), user1.name ne "1")
        assertEquals(user1.name.eq("1"), user1.name eq "1")

        assertEquals(user1.name.equalsIgnoreCase("1"), user1.name eqic "1")

        // Numbers
        assertEquals(user1.id gt 1, user1.id.gt(1))
        assertEquals(user1.id lt 1, user1.id.lt(1))

        // Collections
        val nums: List<Long> = listOf(1, 2, 3)

        assertEquals(
            user1.id.eq(1).or(user1.id.notIn(nums)).and(user1.id.ne(1)).and(user1.id.eq(3)),
            user1.id eq 1 or user1.id notIn nums and user1.id ne 1 and user1.id eq 3
        )

        assertEquals(
            user1.id.`in`(nums).and(user1.id.`in`(listOf(4, 5, 6))),
            user1.id isIn nums and user1.id isIn listOf(4, 5, 6)
        )

        assertEquals(user1.id.notIn(nums), user1.id notIn nums)
    }

}