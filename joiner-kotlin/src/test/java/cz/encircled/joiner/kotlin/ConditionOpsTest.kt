package cz.encircled.joiner.kotlin

import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.query.Q
import kotlin.test.Test
import kotlin.test.assertEquals

class ConditionOpsTest : ConditionOps {

    @Test
    fun collections() {
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

    @Test
    fun strings() {
        assertEquals(user1.name.contains("1"), user1.name contains "1")
        assertEquals(user1.name.ne("1"), user1.name ne "1")
        assertEquals(user1.name.eq("1"), user1.name eq "1")
        assertEquals(user1.name.eq(user1.name), user1.name eq user1.name)

        assertEquals(user1.name.startsWith("1"), user1.name startsWith "1")
        assertEquals(user1.name.startsWith(user1.name), user1.name startsWith user1.name)
        assertEquals(user1.name.startsWithIgnoreCase("1"), user1.name startsWithIc "1")
        assertEquals(user1.name.startsWithIgnoreCase(user1.name), user1.name startsWithIc user1.name)

        assertEquals(user1.name.like("1"), user1.name like "1")
        assertEquals(user1.name.like(user1.name), user1.name like user1.name)
        assertEquals(user1.name.likeIgnoreCase("1"), user1.name likeic "1")
        assertEquals(user1.name.likeIgnoreCase(user1.name), user1.name likeic user1.name)

        assertEquals(user1.name.equalsIgnoreCase("1"), user1.name eqic "1")
    }

    @Test
    fun numbers() {
        assertEquals(user1.id gt 1, user1.id.gt(1))
        assertEquals(user1.id eq 1, user1.id.eq(1))
        assertEquals(user1.id lt 1, user1.id.lt(1))
    }

    @Test
    fun `sub queries`() {
        assertEquals(
            user1.name.eq(Q.select(user1.name).from(user1)).toString(),
            (user1.name eq (user1.name from user1)).toString()
        )

        assertEquals(
            user1.name.ne("").and(user1.name.eq(Q.select(user1.name).from(user1))).toString(),
            (user1.name ne "" and user1.name eq (user1.name from user1)).toString()
        )

        assertEquals(
            user1.name.ne("").and(user1.name.`in`(Q.select(user1.name).from(user1))).toString(),
            (user1.name ne "" and user1.name isIn (user1.name from user1)).toString()
        )

        assertEquals(
            user1.name.ne("").and(user1.name.notIn(Q.select(user1.name).from(user1))).toString(),
            (user1.name ne "" and user1.name notIn (user1.name from user1)).toString()
        )
    }

}