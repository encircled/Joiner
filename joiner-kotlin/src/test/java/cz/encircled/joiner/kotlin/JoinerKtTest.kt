package cz.encircled.joiner.kotlin

import cz.encircled.joiner.kotlin.JoinerKtOps.innerJoin
import cz.encircled.joiner.kotlin.JoinerKtOps.leftJoin
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QGroup
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JoinerKtTest : AbstractTest() {

    lateinit var joinerKt: JoinerKt

    @Before
    fun before() {
        joinerKt = JoinerKt(entityManager)
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

        assertNotNull(find)
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

                    asc QUser.user1.id
        )

        assertNotNull(find)
    }

    @Test
    fun ktCountQueryIntegrationTest() {
        val find = joinerKt.findOne(
            QUser.user1.countOf()
                    leftJoin QGroup.group
                    leftJoin QStatus.status
                    innerJoin QStatus.status

                    where { it.name eq "user1" or it.id ne 1 or it.id isIn listOf(1) }
                    limit 1

                    asc QUser.user1.id
        )

        assertEquals(6, find)
    }

}