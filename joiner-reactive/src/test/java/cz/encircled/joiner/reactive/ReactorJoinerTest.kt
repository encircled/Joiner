package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.exception.JoinerExceptions
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.model.User
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import reactor.test.StepVerifier
import kotlin.test.Test

class ReactorJoinerTest : AbstractReactorTest() {

    @Nested
    inner class Persist {

        @Test
        fun `persist new entity`() {
            val entity = User("Test Name")
            StepVerifier.create(reactorJoiner.persist(entity))
                .expectNextMatches { expectPersistedUser(it, "Test Name") }
                .verifyComplete()
        }

        @Test
        fun `persist entity hibernate exception`() {
            val entity = User("Test Name").apply { id = 3 }
            StepVerifier.create(reactorJoiner.persist(entity))
                .expectErrorMatches { it.message!!.contains("detached entity passed") }
                .verify()

            assertUsersAreEmpty()
        }

        @Test
        fun `persist multiple entities`() {
            StepVerifier.create(reactorJoiner.persist(listOf(User("Test Name"), User("Test Name 2"))))
                .expectNextMatches { expectPersistedUser(it, "Test Name") }
                .expectNextMatches { expectPersistedUser(it, "Test Name 2") }
                .verifyComplete()
        }

        @Test
        fun `persist multiple entities exception`() {
            StepVerifier.create(
                reactorJoiner.persist(
                    listOf(
                        User("1"),
                        User("2").apply { id = 1 },
                        User("3"),
                    )
                )
            )
                .expectErrorMatches { it.message!!.contains("detached entity passed") }
                .verify()

            assertUsersAreEmpty()
        }

    }

    @Nested
    inner class FindOne {

        @Test
        fun `find one success`() {
            createUsers("Test Name")

            StepVerifier.create(reactorJoiner.findOne(user1.name from user1 where { it.name eq "Test Name" }))
                .expectNext("Test Name")
                .verifyComplete()
        }

        @Test
        fun `find one execution exception`() {
            createUsers("Test Name")

            StepVerifier.create(reactorJoiner.findOne(user1.name from QStatus.status))
                .expectErrorMatches { it.message!!.contains("QuerySyntaxException") }
                .verify()
        }

        @Test
        fun `find one empty`() {
            StepVerifier.create(reactorJoiner.findOne(user1.name from user1))
                .expectErrorMatches { it.hasCause(JoinerExceptions.entityNotFound().message!!) }
                .verify()
        }

        @Test
        fun `find one multiple results`() {
            createUsers("Test Name", "Test Name 2")

            StepVerifier.create(reactorJoiner.findOne(user1.name from user1))
                .expectError(JoinerException::class.java)
                .verify()
        }

        @Test
        fun `count query success`() {
            StepVerifier.create(reactorJoiner.findOne(user1.countOf()))
                .expectNext(0)
                .verifyComplete()

            createUsers()

            StepVerifier.create(reactorJoiner.findOne(user1.countOf()))
                .expectNext(2)
                .verifyComplete()

            StepVerifier.create(reactorJoiner.findOne(user1.countOf() where { it.name ne "1" }))
                .expectNext(1)
                .verifyComplete()
        }

    }

    @Nested
    inner class FindOneOptional {

        @Test
        fun `find one success`() {
            createUsers("Test Name")

            StepVerifier.create(reactorJoiner.findOneOptional(user1.name from user1 where { it.name eq "Test Name" }))
                .expectNext("Test Name")
                .verifyComplete()
        }

        @Test
        fun `find one empty`() {
            StepVerifier.create(reactorJoiner.findOneOptional(user1.name from user1))
                .verifyComplete()
        }

        @Test
        fun `find one multiple results`() {
            createUsers("Test Name", "Test Name 2")

            StepVerifier.create(reactorJoiner.findOneOptional(user1.name from user1))
                .expectError(JoinerException::class.java)
                .verify()
        }

        @Test
        fun `count query success`() {
            StepVerifier.create(reactorJoiner.findOneOptional(user1.countOf()))
                .expectNext(0)
                .verifyComplete()

            createUsers()

            StepVerifier.create(reactorJoiner.findOneOptional(user1.countOf()))
                .expectNext(2)
                .verifyComplete()

            StepVerifier.create(reactorJoiner.findOneOptional(user1.countOf() where { it.name ne "1" }))
                .expectNext(1)
                .verifyComplete()
        }

    }

    @Nested
    inner class FindMultiple {

        @Test
        fun `find multiple success with limit and offset`() {
            createUsers("LimitAndOffset", "LimitAndOffset 2", "LimitAndOffset 3")

            StepVerifier.create(reactorJoiner.find(user1.name from user1 where { it.name contains "LimitAndOffset" } limit 1 offset 1))
                .expectNext("LimitAndOffset 2")
                .verifyComplete()
        }

        @Test
        fun `find multiple success`() {
            createUsers("Test Name", "Test Name 2")

            StepVerifier.create(reactorJoiner.find(user1.name from user1 where { it.name eq "Test Name" or it.name eq "Test Name 2" }))
                .expectNext("Test Name")
                .expectNext("Test Name 2")
                .verifyComplete()
        }

        @Test
        fun `find multiple exception`() {
            StepVerifier.create(reactorJoiner.find(user1.name from QStatus.status))
                .expectErrorMatches { it.message!!.contains("QuerySyntaxException") }
                .verify()
        }

    }

    private fun expectPersistedUser(user: User, name: String): Boolean {
        StepVerifier.create(reactorJoiner.findOne(user1 from user1 where { it.id eq user.id }).map { it.name })
            .expectNext(name)
            .verifyComplete()

        return user.id != null && user.name.equals(name)
    }

}
