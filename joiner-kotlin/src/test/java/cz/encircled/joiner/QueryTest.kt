package cz.encircled.joiner

import cz.encircled.joiner.kotlin.QueryBuilder.select
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.*
import org.junit.Assert
import org.junit.Test

/**
 * @author Vlad on 05-Jun-18.
 */
class QueryTest {

    @Test
    fun test() {
        val query =
                select(QUser.user1) {
                    where { it.name.eq("Test") }
                    asc { it.id }

                    innerJoin(QStatus.status, QAddress.address)

                    leftJoin(QPhone.phone)

                    leftJoin(QGroup.group) {
                        leftJoin(QStatus.status) {
                            leftJoin(QUser.user1) {
                                innerJoin(QAddress.address, QPhone.phone)
                                leftJoin(QPhone.phone)
                            }
                        }
                    }

                }

        val userTree = J.left(QUser.user1).nested(QAddress.address, QPhone.phone)

        val expected = Q.from(QUser.user1)
                .where(QUser.user1.name.eq("Test"))
                .asc(QUser.user1.id)
                .joins(J.inner(QStatus.status),
                        J.inner(QAddress.address),

                        J.left(QPhone.phone),

                        J.left(QGroup.group)
                                .nested(J.left(QStatus.status).nested(userTree))
                )
        Assert.assertEquals(expected, query)
        Assert.assertEquals(J.unrollChildrenJoins(expected.joins), J.unrollChildrenJoins(query.joins))
    }

}