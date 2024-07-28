package cz.encircled.joiner.kotlin

import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.mappingTo
import cz.encircled.joiner.model.QUser.user1
import org.junit.jupiter.api.TestInfo
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProjectionKtTest : AbstractTest() {

    lateinit var joinerKt: JoinerKt

    @BeforeTest
    fun before(test: TestInfo) {
        super.beforeEach(test)
        joinerKt = JoinerKt(entityManager)
    }

    @Test
    fun selectAll() {
        val user = joinerKt.getOne(user1.all() where { it.name eq "user1" })
        val user2 = joinerKt.getOne(from(user1) where { it.name eq "user1" })
        assertEquals("user1", user.name)
        assertEquals("user1", user2.name)
    }

    @Test
    fun mapToSingleObject() {
        val names = joinerKt.find(user1.name from user1)
        assertTrue(names.isNotEmpty())
        assertTrue(names.contains("user1"))
    }

    @Test
    fun mapToTuple() {
        val tuple = joinerKt.getOne(listOf(user1.id, user1.name) from user1 where { it.name eq "user1" })
        assertEquals("user1", tuple.get(user1.name))
    }

    @Test
    fun mapToDto() {
        val dto = joinerKt.getOne(
            listOf(user1.id, user1.name)
                    mappingTo IdAndName::class
                    from user1
                    where (user1.name eq "user1")
        )
        assertEquals("user1", dto.name)
    }

    data class IdAndName(val id: Long, val name: String)

}