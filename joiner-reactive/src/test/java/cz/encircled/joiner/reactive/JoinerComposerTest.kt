package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.model.QGroup
import cz.encircled.joiner.model.QStatus
import cz.encircled.joiner.model.QUser.user1
import cz.encircled.joiner.model.User
import cz.encircled.joiner.reactive.composer.JoinerComposer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import reactor.test.StepVerifier.Step
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class JoinerComposerTest : AbstractReactorTest() {

    @Test
    fun `persist chain`() {
        try {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("2"))
                    .persist(User("3"))
                    .findOne((user1.name from user1).where(user1.name eq "1"))
            })
                .expectNext("1")
                .verifyComplete()
        } catch (e: Throwable) {
            e.printStackTrace()
            throw e
        }
    }

    /*@Test
    fun `empty composer`() {
        assertThrows<IllegalStateException> {
            JoinerComposer<Any, Any, Mono<Any>>(ArrayList()).executeChain(reactorJoiner)
        }
    }

    @Test
    fun `multiple chains in single composer`() {
        assertThrows<IllegalStateException> {
            reactorJoiner.transaction {
                find(user1.all())
                find(user1.all())
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
                    .find(user1.name from user1)
            })
                .expectNext("1")
                .expectNext("2")
                .expectNext("3")
                .verifyComplete()
        }

        @Test
        fun `persist single entity`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .verifyComplete()
        }

        @Test
        fun `test delete me`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOne { u ->
                        user1.all() where { it.id eq u.id }
                    }
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .verifyComplete()
        }

        @Test
        fun `persist multiple entities`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(listOf(User("1"), User("2")))
            })
                .expectNextMatches { it.name.equals("1") && it.id != null }
                .expectNextMatches { it.name.equals("2") && it.id != null }
                .verifyComplete()
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
                .verifyComplete()

            StepVerifier.create(reactorJoiner.findOne(user1.countOf()))
                .expectNext(3L)
                .verifyComplete()
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
        fun `initial just`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(1)
            })
                .expectNext(1)
                .verifyComplete()
        }

        @Test
        fun `initial just chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                just("1")
                    .persist { User(it) }
            })
                .expectNextMatches { it.name == "1" && it.id != null }
                .verifyComplete()
        }


        @Test
        fun `mono success find one`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName 2"))
                    .findOne { user ->
                        user1.all() where { it.id eq user.id }
                    }
            })
                .expectNextMatches { user -> user.name.equals("TestName 2") }
                .verifyComplete()
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
                    .findOne(user1.all())
                    .map { it.name }
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()

            assertUsersAreEmpty()
        }

        @Test
        fun `mono error null result`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOne(user1.all())
                    .map { it.name }
            })
                .expectErrorMatches { it.hasCause("FindOne returned no result") }
                .verify()
        }

        @Test
        fun `mono flatMap result`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .flatMap { Mono.just(it.name) }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .verifyComplete()
        }

        @Test
        fun `mono flatMap exception`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .flatMap { Mono.error(JoinerException("flat map")) }
            }
            StepVerifier.create(transaction)
                .expectErrorMatches { it.message!!.contains("flat map") }
                .verify()
        }

        @Test
        fun `mono flatMap from optional`() {
            val transaction = reactorJoiner.transaction {
                findOneOptional(user1.all())
                    .flatMap {
                        assertFalse(it.isPresent)
                        Mono.just("just")
                    }
            }
            StepVerifier.create(transaction)
                .expectNext("just")
                .verifyComplete()
        }

        @Test
        fun `mono flatMap empty`() {
            val transaction = reactorJoiner.transaction {
                findOne(user1.all())
                    .flatMap { Mono.empty() }
            }
            StepVerifier.create(transaction)
                .expectErrorMatches { it.hasCause("FindOne returned no result") }
                .verify()
        }

        @Test
        fun `mono flatMap intermediate`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .flatMap { Mono.just(it.name) }
                    .findOne { name -> user1.all() where { it.name eq name } }
                    .flatMap { Mono.just(it.name) }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .verifyComplete()
        }

    }

    @Nested
    inner class FluxResult {

        @Test
        fun `basic flux`() {
            createUsers()

            StepVerifier.create(reactorJoiner.transaction {
                find(user1.name from user1)
            })
                .expectNext("1")
                .expectNext("2")
                .verifyComplete()
        }

        @Test
        fun `flux collect to list result`() {
            createUsers()

            StepVerifier.create(reactorJoiner.transaction {
                find(user1.name from user1)
                    .collectToList()
            })
                .expectNext(listOf("1", "2"))
                .verifyComplete()
        }

        @Test
        fun `flux empty collect to list`() {
            StepVerifier.create(reactorJoiner.transaction {
                find(user1.name from user1)
            })
                .verifyComplete()
        }

        @Test
        fun `flux collect to list intermediate`() {
            createUsers()

            StepVerifier.create(reactorJoiner.transaction {
                find(user1.name from user1)
                    .collectToList()
                    .find { names -> user1.all() where { it.name isIn names } }
            })
                .expectNextMatches { it.name == "1" && it.id != null }
                .expectNextMatches { it.name == "2" && it.id != null }
                .verifyComplete()
        }

        @Test
        fun `basic flux chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("TestName Flux 1"))
                    .persist { user -> User("${user.name}2") }
                    .find { user1.name from user1 where { it.name contains "Flux" } }
            })
                .expectNext("TestName Flux 1")
                .expectNext("TestName Flux 12")
                .verifyComplete()
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
                    .find { user1.id from user1 where { it.name contains "Flux" } }
                    .find { ids -> user1.name from user1 where { it.id isIn ids } }
            })
                .expectNext("TestName Flux 1")
                .expectNext("TestName Flux 12")
                .verifyComplete()
        }

        @Test
        fun `flux map result`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName2"))
                    .find { user1.all() }
                    .map { it.name }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .expectNext("TestName2")
                .verifyComplete()
        }

        @Test
        fun `flux flatMap result`() {
            val transaction = reactorJoiner.transaction {
                persist(User("TestName"))
                    .persist(User("TestName2"))
                    .find { user1.all() }
                    .flatMap { Mono.just(it.name) }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .expectNext("TestName2")
                .verifyComplete()
        }

        @Test
        fun `flux flatMap intermediate`() {
            val transaction = reactorJoiner.transaction {
                persist(listOf(User("TestName"), User("TestName2")))
                    .find { user1.all() }
                    .flatMap { Mono.just(it.name) }
                    .find { names -> user1.name from user1 where { it.name isIn names } }
            }
            StepVerifier.create(transaction)
                .expectNext("TestName")
                .expectNext("TestName2")
                .verifyComplete()
        }

        @Test
        fun `flux flatMap from empty`() {
            val transaction = reactorJoiner.transaction {
                find(user1.all())
                    .flatMap { Mono.just(it.name) }
                    .map { it }
            }
            StepVerifier.create(transaction)

                .verifyComplete()
        }

        @Test
        fun `flux empty flatMap`() {
            val transaction = reactorJoiner.transaction {
                persist(listOf(User("1")))
                    .flatMap { Mono.empty() }
            }
            StepVerifier.create(transaction)
                .verifyComplete()
        }

        @Test
        fun `flux flatMap exception`() {
            val transaction = reactorJoiner.transaction {
                persist(User("1"))
                    .flatMap { Mono.error(JoinerException("flat map")) }
            }
            StepVerifier.create(transaction)
                .expectErrorMatches { it.hasCause("flat map") }
                .verify()
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
                    .find { user1.all() }
                    .map { it.name }
                    .findOne { names -> user1.all() where { it.name eq names[0] } }
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
                    .find { user1.all() }
                    .map { it.name }
                    .find { names -> user1.all() where { it.name isIn names } }
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
                        user1.all()
                    }
            })
                .expectErrorMatches { it.message!!.contains("detached entity passed") }
                .verify()
        }

        @Test
        fun `find all flux result`() {
            createUsers()

            StepVerifier.create(reactorJoiner.transaction {
                persist(User("3"))
                    .find {
                        user1.all()
                    }
            })
                .expectNextMatches { it.name == "1" }
                .expectNextMatches { it.name == "2" }
                .expectNextMatches { it.name == "3" }
                .verifyComplete()
        }

        @Test
        fun `test filter`() {
            StepVerifier.create(reactorJoiner.transaction {
                just(listOf(1, 2, 3, 4))
                    .filter { it % 2 == 0 }
            })
                .expectNext(2)
                .expectNext(4)
                .verifyComplete()
        }

        @Test
        fun `test filter intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(listOf(User("1"), User("2"), User("3")))
                    .filter { it.name != "2" }
                    .map { it.id }
                    .find { ids -> user1.name from user1 where { it.id isIn ids } }
            })
                .expectNext("1")
                .expectNext("3")
                .verifyComplete()
        }

    }

    @Nested
    inner class OptionalMono {

        @Test
        fun `map empty optional to value`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("Test"))
                    .findOneOptional(user1.all() where { it.name eq "Not exists" })
                    .map { o -> o.orElse(User("Test")).name }
                    .findOne { name -> user1.all() where { it.name eq name } }
            })
                .expectNextMatches { it.name.equals("Test") }
                .verifyComplete()
        }

        @Test
        fun `return empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOneOptional(user1.all())
            })
                .expectNext(Optional.empty())
                .verifyComplete()
        }

        @Test
        fun `empty optional in chain`() {
            StepVerifier.create(reactorJoiner.transaction {
                findOneOptional(user1.all())
                    .findOneOptional {
                        assertFalse(it.isPresent)
                        user1.all()
                    }
                    .findOneOptional {
                        assertFalse(it.isPresent)
                        user1.all()
                    }
            })
                .expectNext(Optional.empty())
                .verifyComplete()
        }

        @Test
        fun `optional has value`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from user1)
            })
                .expectNext(Optional.of("1"))
                .verifyComplete()
        }

        @Test
        fun `optional has error`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from QStatus.status)
            })
                .expectErrorMatches { it.message!!.contains("QuerySyntaxException") }
                .verify()
        }

        @Test
        fun `optional has value intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from user1)
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
                    .findOneOptional(user1.all())
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()
        }

        @Test
        fun `optional has multiple values intermediate`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .persist(User("2"))
                    .findOneOptional(user1.all())
                    .map { it.get() }
            })
                .expectErrorMatches { it.hasCause("FindOne returned multiple result") }
                .verify()
        }

        @Test
        fun `optional has value to map`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from user1)
                    .map { "${it.get()}1" }
            })
                .expectNext("11")
                .verifyComplete()
        }

        @Test
        fun `findOne after empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from user1 where { it.name eq "2" })
                    .findOne { optional -> user1.all() where { user1.name eq optional.orElse("1") } }
            })
                .expectNextMatches { it.name == "1" }
                .verifyComplete()
        }

        @Test
        fun `find after empty optional`() {
            StepVerifier.create(reactorJoiner.transaction {
                persist(User("1"))
                    .findOneOptional(user1.name from user1 where { it.name eq "2" })
                    .find { optional -> user1.all() where { user1.name eq optional.orElse("1") } }
            })
                .expectNextMatches { it.name == "1" }
                .verifyComplete()
        }

    }*/

}
