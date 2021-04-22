package cz.encircled.joiner.kotlin

import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.QueryBuilder.all
import cz.encircled.joiner.kotlin.QueryBuilder.countOf
import cz.encircled.joiner.kotlin.QueryBuilder.from
import cz.encircled.joiner.model.*
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 05-Jun-18.
 */
class QueryBuilderTest {

    @Test
    fun testCountQuery() {
        assertEquals(
            (QUser.user1.countOf()).delegate,
            Q.count(QUser.user1)
        )
    }

    @Test
    fun testCustomProjection() {
        assertEquals(
            (QUser.user1.name from QUser.user1).delegate,
            Q.select(QUser.user1.name).from(QUser.user1)
        )
    }

    @Test
    fun testDefaultAllProjection() {
        assertEquals(
            (QUser.user1.all()).delegate,
            Q.from(QUser.user1)
        )
    }

    @Test
    fun testOperators() {
        KConditionOps().apply {
            assertEquals(
                QUser.user1.id.eq(1).or(QUser.user1.id.notIn(listOf(1, 2, 3))).and(QUser.user1.id.ne(1))
                    .and(QUser.user1.id.eq(3)),
                QUser.user1.id eq 1 or QUser.user1.id notIn listOf(
                    1,
                    2,
                    3
                ) and QUser.user1.id ne 1 and QUser.user1.id eq 3
            )

            assertEquals(
                QUser.user1.id.`in`(listOf(1, 2, 3)).and(QUser.user1.id.`in`(listOf(4, 5, 6))),
                QUser.user1.id isIn listOf(1, 2, 3) and QUser.user1.id isIn listOf(4, 5, 6)
            )

            assertEquals(
                QUser.user1.id.notIn(listOf(1, 2, 3)),
                QUser.user1.id notIn listOf(1, 2, 3)
            )
            assertEquals(
                QUser.user1.id.ne(1),
                QUser.user1.id ne 1
            )
        }
    }

    @Test
    fun testPredicateWithChainedInfix() {
        assertEquals(
            (
                    QUser.user1.name from QUser.user1
                            where {
                        it.name eq "1" and it.id.eq(2) or it.name.eq("3") or (it.id eq 4 or it.name ne "5" or it.name.eq(
                            "6"
                        ))
                    }
                    ).delegate,
            Q.select(QUser.user1.name).from(QUser.user1)
                .where(
                    QUser.user1.name.eq("1").and(QUser.user1.id.eq(2)).or(
                        QUser.user1.name.eq("3")
                    ).or(
                        QUser.user1.id.eq(4).or(QUser.user1.name.ne("5")).or(
                            QUser.user1.name.eq("6")
                        )
                    )
                )
        )
    }

    @Test
    fun testSimpleJoins() {
        val delegate = (QUser.user1.name from QUser.user1
                leftJoin QContact.contact
                innerJoin QStatus.status
                ).delegate

        assertEquals(
            Q.select(QUser.user1.name).from(QUser.user1)
                .joins(J.left(QContact.contact))
                .joins(J.inner(QStatus.status)),
            delegate
        )
    }

    @Test
    fun testNestedJoins() {
        val delegate = (QUser.user1.name from QUser.user1
                leftJoin (QContact.contact leftJoin QStatus.status innerJoin QUser.user1)
                leftJoin (QGroup.group innerJoin QStatus.status)
                innerJoin QStatus.status
                innerJoin (QAddress.address innerJoin QStatus.status)
                ).delegate

        assertEquals(
            Q.select(QUser.user1.name).from(QUser.user1)
                .joins(J.left(QContact.contact).nested(J.left(QStatus.status).nested(J.inner(QUser.user1))))
                .joins(J.left(QGroup.group).nested(J.inner(QStatus.status)))
                .joins(J.inner(QStatus.status))
                .joins(J.inner(QAddress.address).nested(J.inner(QStatus.status))),
            delegate
        )
    }

    @Test
    fun testOrdering() {
        assertEquals(
            Q.select(QUser.user1).from(QUser.user1).asc(QUser.user1.id),
            (QUser.user1 from QUser.user1 asc { it.id }).delegate
        )
        assertEquals(
            Q.select(QUser.user1).from(QUser.user1).desc(QUser.user1.id),
            (QUser.user1 from QUser.user1 desc { it.id }).delegate
        )
    }

    @Test
    fun testComplexQuery() {
        val query =
            (QUser.user1 from QUser.user1
                    where { it.name eq "Test" }
                    asc { it.id }

                    innerJoin QStatus.status
                    innerJoin QAddress.address
                    leftJoin QPhone.phone

                    leftJoin (
                    QGroup.group leftJoin (QStatus.status innerJoin (QUser.user1 leftJoin QAddress.address leftJoin QPhone.phone))
                    )
                    )

        val userTree = J.inner(QUser.user1).nested(QAddress.address, QPhone.phone)

        val expected = Q.from(QUser.user1)
            .where(QUser.user1.name.eq("Test"))
            .asc(QUser.user1.id)
            .joins(
                J.inner(QStatus.status),
                J.inner(QAddress.address),

                J.left(QPhone.phone),

                J.left(QGroup.group)
                    .nested(J.left(QStatus.status).nested(userTree))
            )
        Assert.assertEquals(expected, query.delegate)
        Assert.assertEquals(J.unrollChildrenJoins(expected.joins), J.unrollChildrenJoins(query.joins))
    }

}