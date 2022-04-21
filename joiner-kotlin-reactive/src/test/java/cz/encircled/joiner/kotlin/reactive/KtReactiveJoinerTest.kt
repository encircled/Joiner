package cz.encircled.joiner.kotlin.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.exception.JoinerExceptions
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.model.User
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KtReactiveJoinerTest : WithInMemMySql() {

    @Test
    fun `save and get`() = runBlocking {
        val persisted = joiner.persist(User("KtTest"))
        assertNotNull(persisted.id)
        assertEquals("KtTest", persisted.name)

        val found = joiner.find(user1.id from user1 where { it.name eq "KtTest" })
        assertEquals(1, found.size)
        assertEquals(persisted.id, found[0])

        val foundOne = joiner.findOne(user1.id from user1 where { it.name eq "KtTest" })
        assertEquals(persisted.id, foundOne)
    }

    @Test
    fun `save and remove`() = runBlocking {
        val persisted = joiner.persist(User("KtTest"))
        assertNotNull(persisted.id)
        assertEquals("KtTest", persisted.name)

        joiner.remove(persisted)
        assertTrue(joiner.find(user1.all()).isEmpty())
    }

    @Test
    fun `save and get multiple`() {
        runBlocking {
            val persisted = joiner.persist(listOf(User("1"), User("2"), User("3")))
            assertEquals(3, persisted.size)

            val found = joiner.find(user1.name from user1 where { it.name isIn listOf("1", "3") })
            assertEquals(listOf("1", "3"), found)

            val foundOne = joiner.findOne(user1.name from user1 where { it.name eq "1" })
            assertEquals("1", foundOne)

            assertThrows<JoinerException>(JoinerExceptions.multipleEntitiesFound().message!!) {
                runBlocking {
                    joiner.findOne(user1.name from user1 where { it.name isIn listOf("1", "2") })
                }
            }
        }
    }

}