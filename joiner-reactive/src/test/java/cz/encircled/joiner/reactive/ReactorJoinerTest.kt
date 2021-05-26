package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import org.junit.Test
import reactor.test.StepVerifier


class ReactorJoinerTest : WithInMemMySql() {

    @Test
    fun testPersistNewEntity() {
        val entity = User("Test Name")
        StepVerifier.create(reactorJoiner.persist(entity))
            .expectNextMatches { expectPersistedUser(it, "Test Name") }
            .expectComplete()
            .verify()
    }

    @Test
    fun testPersistMultipleEntities() {
        StepVerifier.create(reactorJoiner.persist(listOf(User("Test Name"), User("Test Name 2"))))
            .expectNextMatches { expectPersistedUser(it, "Test Name") }
            .expectNextMatches { expectPersistedUser(it, "Test Name 2") }
            .expectComplete()
            .verify()
    }

    @Test
    fun testFindOne() {
        reactorJoiner.persist(User("Test Name")).block()

        StepVerifier.create(reactorJoiner.findOne(QUser.user1.name from QUser.user1 where { it.name eq "Test Name" }))
            .expectNext("Test Name")
            .expectComplete()
            .verify()
    }

    @Test
    fun testFindOneMultipleResults() {
        reactorJoiner.persist(User("Test Name")).block()
        reactorJoiner.persist(User("Test Name 2")).block()

        StepVerifier.create(reactorJoiner.findOne(QUser.user1.name from QUser.user1))
            .expectError(JoinerException::class.java)
            .verify()
    }

    @Test
    fun testFindMultiple() {
        reactorJoiner.persist(User("Test Name")).block()
        reactorJoiner.persist(User("Test Name 2")).block()

        StepVerifier.create(reactorJoiner.find(QUser.user1.name from QUser.user1 where { it.name eq "Test Name" or it.name eq "Test Name 2" }))
            .expectNext("Test Name")
            .expectNext("Test Name 2")
            .expectComplete()
            .verify()
    }

    private fun expectPersistedUser(user: User, name: String): Boolean {
        return user.id != null && user.name.equals(name) &&
                reactorJoiner.findOne(QUser.user1 from QUser.user1 where { it.id eq user.id }).block() != null
    }

}
