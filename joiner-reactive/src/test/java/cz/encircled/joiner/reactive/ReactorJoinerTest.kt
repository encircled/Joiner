package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import reactor.test.StepVerifier
import kotlin.test.Test


class ReactorJoinerTest : WithInMemMySql() {

    @Test
    fun testTransactionBatchAsMono() {
        StepVerifier.create(reactorJoiner.transaction {
            persist(User("TestName"))
                .persist(User("TestName 2"))
                .findOne { user ->
                    QUser.user1.all() where { it.id eq user.id }
                }
        })
            .expectNextMatches { user -> user.name.equals("TestName 2") }
            .expectComplete()
            .verify()
    }

    @Test
    fun testTransactionBatchAsFlux() {
        StepVerifier.create(reactorJoiner.transaction {
            persist(User("TestName Flux 1"))
                .persist { user -> User("${user.name}2") }
                .find { QUser.user1.name from QUser.user1 where { it.name contains "Flux"  } }
        })
            .expectNext("TestName Flux 1")
            .expectNext("TestName Flux 12")
            .expectComplete()
            .verify()
    }

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
    fun testFindMultipleLimitAndOffset() {
        reactorJoiner.persist(User("Test Name")).block()
        reactorJoiner.persist(User("Test Name 2")).block()
        reactorJoiner.persist(User("Test Name 3")).block()

        StepVerifier.create(reactorJoiner.find(QUser.user1.name from QUser.user1 limit 1 offset 1))
            .expectNext("Test Name 2")
            .expectComplete()
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
