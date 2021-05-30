package cz.encircled.joiner.kotlin

import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.*
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Vlad on 05-Jun-18.
 */
class JoinerKtQueryBuilderTest {

    @Test
    fun `count query`() {
        assertEquals(
            (user1.countOf()).delegate,
            Q.count(user1)
        )
    }

    @Test
    fun `custom projection`() {
        assertEquals(
            (user1.name from user1).delegate,
            Q.select(user1.name).from(user1)
        )
    }

    @Test
    fun `default all projection`() {
        assertEquals(
            (user1.all()).delegate,
            Q.from(user1)
        )
    }

    @Test
    fun `predicate with chained infix`() {
        assertEquals(
            (user1.name from user1
                    where {
                it.name eq "1" and it.id.eq(2) or it.name.eq("3") or (it.id eq 4 or it.name ne "5" or
                        it.name.eq("6"))
            }).delegate,

            Q.select(user1.name).from(user1)
                .where(
                    user1.name.eq("1").and(user1.id.eq(2)).or(
                        user1.name.eq("3")
                    ).or(
                        user1.id.eq(4).or(user1.name.ne("5")).or(
                            user1.name.eq("6")
                        )
                    )
                )
        )
    }

    @Test
    fun `root left and inner join`() {
        val delegate = (user1.name from user1
                leftJoin QContact.contact
                innerJoin QStatus.status
                ).delegate

        assertEquals(
            Q.select(user1.name).from(user1)
                .joins(J.left(QContact.contact))
                .joins(J.inner(QStatus.status)),
            delegate
        )
    }

    @Test
    fun `nested left and inner joins`() {
        val delegate = (user1.name from user1
                leftJoin (QContact.contact leftJoin QStatus.status innerJoin user1)
                leftJoin (QGroup.group innerJoin QStatus.status)
                innerJoin QStatus.status
                innerJoin (QAddress.address innerJoin QStatus.status)
                ).delegate

        assertEquals(
            Q.select(user1.name).from(user1)
                .joins(J.left(QContact.contact).nested(J.left(QStatus.status).nested(J.inner(user1))))
                .joins(J.left(QGroup.group).nested(J.inner(QStatus.status)))
                .joins(J.inner(QStatus.status))
                .joins(J.inner(QAddress.address).nested(J.inner(QStatus.status))),
            delegate
        )
    }

    @Test
    fun `ordering`() {
        assertEquals(
            Q.select(user1).from(user1).asc(user1.id),
            (user1 from user1 asc { it.id }).delegate
        )
        assertEquals(
            Q.select(user1).from(user1).desc(user1.id),
            (user1 from user1 desc { it.id }).delegate
        )
    }

    @Test
    fun `complex query`() {
        val query =
            (user1 from user1
                    where { it.name eq "Test" }
                    asc { it.id }

                    innerJoin QStatus.status
                    innerJoin QAddress.address
                    leftJoin QPhone.phone

                    leftJoin (
                    QGroup.group leftJoin (QStatus.status innerJoin (user1 leftJoin QAddress.address leftJoin QPhone.phone))
                    )
                    )

        val userTree = J.inner(user1).nested(QAddress.address, QPhone.phone)

        val expected = Q.from(user1)
            .where(user1.name.eq("Test"))
            .asc(user1.id)
            .joins(
                J.inner(QStatus.status),
                J.inner(QAddress.address),

                J.left(QPhone.phone),

                J.left(QGroup.group)
                    .nested(J.left(QStatus.status).nested(userTree))
            )
        assertEquals(expected, query.delegate)
        assertEquals(J.unrollChildrenJoins(expected.joins), J.unrollChildrenJoins(query.joins))
    }

}