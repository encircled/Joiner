package cz.encircled.joiner.reactive

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.exception.JoinerExceptions
import cz.encircled.joiner.reactive.ReactorExtension.getAtMostOne
import cz.encircled.joiner.reactive.ReactorExtension.getExactlyOne
import cz.encircled.joiner.reactive.ReactorExtension.reactor
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.assertThrows
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.ExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReactorExtensionTest {

    @Test
    fun `reactor mono value propagated`() {
        val mono = Mono.create<Int> { mono ->
            reactor(mono) {
                mono.success(1)
            }
        }

        StepVerifier.create(mono)
            .expectNext(1)
            .verifyComplete()
    }

    @Test
    fun `reactor mono exception propagated`() {
        val mono = Mono.create<Int> { mono ->
            reactor(mono) {
                throw IllegalStateException("Test")
            }
        }

        StepVerifier.create(mono)
            .expectErrorMatches { it.message!! == "Test" }
            .verify()
    }

    @Test
    fun `reactor flux value propagated`() {
        val flux = Flux.create<Int> { flux ->
            reactor(flux) {
                flux.next(1)
                flux.next(2)
                flux.complete()
            }
        }

        StepVerifier.create(flux)
            .expectNext(1)
            .expectNext(2)
            .verifyComplete()
    }

    @Test
    fun `reactor flux exception propagated`() {
        val flux = Flux.create<Int> { flux ->
            reactor(flux) {
                throw IllegalStateException("Test")
            }
        }

        StepVerifier.create(flux)
            .expectErrorMatches { it.message!! == "Test" }
            .verify()
    }

    @Test
    fun `reactor completable future exception propagated`() {
        val reactor = reactor<String> {
            throw java.lang.IllegalStateException("Test")
        }
        assertThrows<ExecutionException>("Test") {
            reactor.get()
        }
    }

    @Test
    fun `get exactly one from list`() {
        assertEquals(1, listOf(1).getExactlyOne())

        assertThrows<JoinerException>(JoinerExceptions.entityNotFound().message!!) {
            listOf<String>().getExactlyOne()
        }

        assertThrows<JoinerException>(JoinerExceptions.multipleEntitiesFound().message!!) {
            listOf(1, 2).getExactlyOne()
        }
    }

    @Test
    fun `get at most one from list`() {
        assertEquals(1, listOf(1).getAtMostOne())

        assertNull(listOf<String>().getAtMostOne())
        assertNull((null as List<String>?).getAtMostOne())

        assertThrows<JoinerException>(JoinerExceptions.multipleEntitiesFound().message!!) {
            listOf(1, 2).getAtMostOne()
        }
    }

}