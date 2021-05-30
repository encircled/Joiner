package cz.encircled.joiner.reactive

import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser
import cz.encircled.joiner.model.User
import cz.encircled.joiner.reactive.composer.JoinerComposer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class JoinerComposerTest : AbstractReactorTest() {

    @Test
    fun `empty composer`() {
        assertThrows<IllegalStateException> {
            JoinerComposer<Any, Any, Mono<Any>>(ArrayList()).executeChain(reactorJoiner)
        }
    }

    @Test
    fun `multiple chains in single composer`() {
        // This must work with no restrictions
        reactorJoiner.transaction {
            find(QUser.user1.all())
                .find(QUser.user1.all())
        }.collectList().block()

        assertThrows<IllegalStateException> {
            reactorJoiner.transaction {
                find(QUser.user1.all())
                find(QUser.user1.all())
            }
            Unit
        }
    }

    @Nested
    inner class Persist {

        @Test
        fun `persist chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("2"))
                    .persist(User("3"))
                    .find(QUser.user1.name from QUser.user1)
            })
                .expectNext("1")
                .expectNext("2")
                .expectNext("3")
                .expectComplete()
                .verify()
        }

        @Test
        fun `persist single entity`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .expectComplete()
                .verify()
        }

        @Test
        fun `test delete me`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOne { u ->
                        QUser.user1.all() where { it.id eq u.id }
                    }
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .expectComplete()
                .verify()
        }

        @Test
        fun `persist multiple entities`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(listOf(User("1"), User("2")))
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .expectNextMatches { it.name.equals("2") && it.id != null }
                .expectComplete()
                .verify()
        }

        @Test
        fun `persist multiple entities intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persistMultiple {
                        listOf(User("2"), User("3"))
                    }
            })
                .expectNextMatches {
                    it.name.equals("2") && it.id != null
                }
                .expectNextMatches {
                    it.name.equals("3") && it.id != null
                }
                .expectComplete()
                .verify()

            assertEquals(3, reactorJoiner.findOne(QUser.user1.countOf()).block())
        }

        @Test
        fun `persist multiple entities exception`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(listOf(User("1"), User("2").apply { id = 1 }, User("3")))
            })
                .expectErrorMatches { it.message!!.contains("detached entity passed") }
                .verify()

            assertUsersAreEmpty()
        }

    }

    @Nested
    inner class MonoResult {

        @Test
        fun `initial just list`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(1)
            })
                .expectNext(1)
                .verifyComplete()
        }

        @Test
        fun `initial just chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(listOf("1", "2"))
                    .persistMultiple { it.map { name -> User(name) } }
            })
                .expectNextMatches { it.name == "1" && it.id != null }
                .expectNextMatches { it.name == "2" && it.id != null }
                .verifyComplete()
        }


        @Test
        fun `mono success find one`() {
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
        fun `mono success map`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName"))
                    .map { it.name }
            })
                .expectNext("TestName")
                .verifyComplete()
        }

        @Test
        fun `mono error multiple results`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName"))
                    .findOne(QUser.user1.all())
                    .map { it.name }
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()

            assertUsersAreEmpty()
        }

        @Test
        fun `mono error null result`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOne(QUser.user1.all())
                    .map { it.name }
            })
                .expectErrorMatches { it.hasCause("FindOne returned no result") }
                .verify()
        }

    }

    @Nested
    inner class FluxResult {

        @Test
        fun `basic flux`() {
            reactorJoiner.persist(listOf(User("1"), User("2"))).collectList().block()

            StepVerifier.create(reactorJoiner.transaction {
                find(QUser.user1.name from QUser.user1)
            })
                .expectNext("1")
                .expectNext("2")
                .expectComplete()
                .verify()
        }

        @Test
        fun `flux collect to list result`() {
            reactorJoiner.persist(listOf(User("1"), User("2"))).collectList().block()

            StepVerifier.create(reactorJoiner.transaction {
                find(QUser.user1.name from QUser.user1)
                    .collectToList()
            })
                .expectNext(listOf("1", "2"))
                .expectComplete()
                .verify()
        }

        @Test
        fun `flux empty collect to list`() {
            StepVerifier.create(reactorJoiner.transaction {
                find(QUser.user1.name from QUser.user1)
            })
                .expectComplete()
                .verify()
        }

        @Test
        fun `flux collect to list intermediate`() {
            reactorJoiner.persist(listOf(User("1"), User("2"))).collectList().block()

            StepVerifier.create(reactorJoiner.transaction {
                find(QUser.user1.name from QUser.user1)
                    .collectToList()
                    .find { names -> QUser.user1.all() where { it.name isIn names } }
            })
                .expectNextMatches { it.name == "1" && it.id != null }
                .expectNextMatches { it.name == "2" && it.id != null }
                .expectComplete()
                .verify()
        }

        @Test
        fun `basic flux chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName Flux 1"))
                    .persist { user -> User("${user.name}2") }
                    .find { QUser.user1.name from QUser.user1 where { it.name contains "Flux" } }
            })
                .expectNext("TestName Flux 1")
                .expectNext("TestName Flux 12")
                .expectComplete()
                .verify()
        }

        @Test
        fun `initial just list`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(listOf(1, 2))
            })
                .expectNext(1)
                .expectNext(2)
                .verifyComplete()
        }

        @Test
        fun `initial just chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(listOf("1", "2"))
                    .persistMultiple { it.map { name -> User(name) } }
            })
                .expectNextMatches { it.name == "1" && it.id != null }
                .expectNextMatches { it.name == "2" && it.id != null }
                .verifyComplete()
        }

        @Test
        fun `basic flux chain plural to plural`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName Flux 1"))
                    .persist { user -> User("${user.name}2") }
                    .find { QUser.user1.id from QUser.user1 where { it.name contains "Flux" } }
                    .find { ids -> QUser.user1.name from QUser.user1 where { it.id isIn ids } }
            })
                .expectNext("TestName Flux 1")
                .expectNext("TestName Flux 12")
                .expectComplete()
                .verify()
        }

        @Test
        fun `flux map result`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName2"))
                    .find { QUser.user1.all() }
                    .map { it.name }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .expectNext("TestName2")
                .verifyComplete()
        }

        @Test
        fun `flux multiple map and filter`() {
            val transaction = reactorJoiner.transaction {
                persist(listOf(User("TestName"), User("TestName2")))
                    .map { it.name }
                    .filter { true }
                    .map { it }
                    .filter { true }
                    .map { it }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .expectNext("TestName2")
                .verifyComplete()
        }

        @Test
        fun `map to mono intermediate`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName2"))
                    .find { QUser.user1.all() }
                    .map { it.name }
                    .findOne { names -> QUser.user1.all() where { it.name eq names[0] } }
            }
            StepVerifier.create(transaction)
                .expectNextMatches { it.name == "TestName" && it.id != null }
                .verifyComplete()
        }

        @Test
        fun `map to flux intermediate`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName2"))
                    .find { QUser.user1.all() }
                    .map { it.name }
                    .find { names -> QUser.user1.all() where { it.name isIn names } }
            }
            StepVerifier.create(transaction)
                .expectNextMatches { it.name == "TestName" && it.id != null }
                .expectNextMatches { it.name == "TestName2" && it.id != null }
                .verifyComplete()
        }

        @Test
        fun testFluxErrorInsideUserFunction() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("3").apply { id = 1 }) // This should fail during hibernate persist
                    .find {
                        QUser.user1.all()
                    }
            })
                .expectErrorMatches { it.message!!.contains("detached entity passed") }
                .verify()
        }

        @Test
        fun `find all flux result`() {
            reactorJoiner.persist(listOf(User("1"), User("2"))).blockLast()

            StepVerifier.create(reactorJoiner.transaction {
                persist(User("3"))
                    .find {
                        QUser.user1.all()
                    }
            })
                .expectNextMatches { it.name == "1" }
                .expectNextMatches { it.name == "2" }
                .expectNextMatches { it.name == "3" }
                .expectComplete()
                .verify()
        }

        @Test
        fun `test filter`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(listOf(1, 2, 3, 4))
                    .filter { it % 2 == 0 }
            })
                .expectNext(2)
                .expectNext(4)
                .expectComplete()
                .verify()
        }

        @Test
        fun `test filter intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(listOf(User("1"), User("2"), User("3")))
                    .filter { it.name != "2" }
                    .map { it.id }
                    .find { ids -> QUser.user1.name from QUser.user1 where { it.id isIn ids } }
            })
                .expectNext("1")
                .expectNext("3")
                .expectComplete()
                .verify()
        }

    }

    @Nested
    inner class OptionalMono {

        @Test
        fun `map empty optional to value`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("Test"))
                    .findOneOptional(QUser.user1.all() where { it.name eq "Not exists" })
                    .map { o -> o.orElse(User("Test")).name }
                    .findOne { name -> QUser.user1.all() where { it.name eq name } }
            })
                .expectNextMatches { it.name.equals("Test") }
                .verifyComplete()
        }

        @Test
        fun `return empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOneOptional(QUser.user1.all())
            })
                .expectNext(Optional.empty())
                .verifyComplete()
        }

        @Test
        fun `empty optional in chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOneOptional(QUser.user1.all())
                    .findOneOptional {
                        assertFalse(it.isPresent)
                        QUser.user1.all()
                    }
                    .findOneOptional {
                        assertFalse(it.isPresent)
                        QUser.user1.all()
                    }
            })
                .expectNext(Optional.empty())
                .verifyComplete()
        }

        @Test
        fun `optional has value`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QUser.user1)
            })
                .expectNext(Optional.of("1"))
                .verifyComplete()
        }

        @Test
        fun `optional has error`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QStatus.status)
            })
                .expectErrorMatches { it.message!!.contains("QuerySyntaxException") }
                .verify()
        }

        @Test
        fun `optional has value intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QUser.user1)
                    .map { it.get() }
            })
                .expectNext("1")
                .verifyComplete()
        }

        @Test
        fun `optional has multiple values in result`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("2"))
                    .findOneOptional(QUser.user1.all())
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()
        }

        @Test
        fun `optional has multiple values intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("2"))
                    .findOneOptional(QUser.user1.all())
                    .map { it.get() }
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()
        }

        @Test
        fun `optional has value to map`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QUser.user1)
                    .map { "${it.get()}1" }
            })
                .expectNext("11")
                .verifyComplete()
        }

        @Test
        fun `findOne after empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QUser.user1 where { it.name eq "2" })
                    .findOne { optional -> QUser.user1.all() where { QUser.user1.name eq optional.orElse("1") } }
            })
                .expectNextMatches { it.name == "1" }
                .verifyComplete()
        }

        @Test
        fun `find after empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(QUser.user1.name from QUser.user1 where { it.name eq "2" })
                    .find { optional -> QUser.user1.all() where { QUser.user1.name eq optional.orElse("1") } }
            })
                .expectNextMatches { it.name == "1" }
                .verifyComplete()
        }

    }

}
