package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.exception.JoinerExceptions
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
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
                .expectComplete()
                .verify()
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
                .expectComplete()
                .verify()
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
            reactorJoiner.persist(User("Test Name")).block()

            StepVerifier.create(reactorJoiner.findOne(QUser.user1.name from QUser.user1 where { it.name eq "Test Name" }))
                .expectNext("Test Name")
                .expectComplete()
                .verify()
        }

        @Test
        fun `find one empty`() {
            StepVerifier.create(reactorJoiner.findOne(QUser.user1.name from QUser.user1))
                .expectErrorMatches { it.hasCause(JoinerExceptions.entityNotFound().message!!) }
                .verify()
        }

        @Test
        fun `find one multiple results`() {
            reactorJoiner.persist(User("Test Name")).block()
            reactorJoiner.persist(User("Test Name 2")).block()

            StepVerifier.create(reactorJoiner.findOne(QUser.user1.name from QUser.user1))
                .expectError(JoinerException::class.java)
                .verify()
        }

        @Test
        fun `count query success`() {
            StepVerifier.create(reactorJoiner.findOne(QUser.user1.countOf()))
                .expectNext(0)
                .verifyComplete()

            reactorJoiner.persist(listOf(User("1"), User("2"))).collectList().block()

            StepVerifier.create(reactorJoiner.findOne(QUser.user1.countOf()))
                .expectNext(2)
                .verifyComplete()

            StepVerifier.create(reactorJoiner.findOne(QUser.user1.countOf() where { it.name ne "1" }))
                .expectNext(1)
                .verifyComplete()
        }

    }

    @Nested
    inner class FindMultiple {

        @Test
        fun `find multiple success with limit and offset`() {
            reactorJoiner.persist(User("LimitAndOffset")).block()
            reactorJoiner.persist(User("LimitAndOffset 2")).block()
            reactorJoiner.persist(User("LimitAndOffset 3")).block()

            StepVerifier.create(reactorJoiner.find(QUser.user1.name from QUser.user1 where { it.name contains "LimitAndOffset" } limit 1 offset 1))
                .expectNext("LimitAndOffset 2")
                .expectComplete()
                .verify()
        }

        @Test
        fun `find multiple success`() {
            reactorJoiner.persist(User("Test Name")).block()
            reactorJoiner.persist(User("Test Name 2")).block()

            StepVerifier.create(reactorJoiner.find(QUser.user1.name from QUser.user1 where { it.name eq "Test Name" or it.name eq "Test Name 2" }))
                .expectNext("Test Name")
                .expectNext("Test Name 2")
                .expectComplete()
                .verify()
        }

        @Test
        fun `find multiple exception`() {
            StepVerifier.create(reactorJoiner.find(QUser.user1.name from QStatus.status))
                .expectErrorMatches { it.message!!.contains("QuerySyntaxException") }
                .verify()
        }

    }

    private fun expectPersistedUser(user: User, name: String): Boolean {
        return user.id != null && user.name.equals(name) &&
                reactorJoiner.findOne(QUser.user1 from QUser.user1 where { it.id eq user.id }).block() != null
    }

}
