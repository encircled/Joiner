package cz.encircled.joiner.reactive

import cz.encircled.joiner.reactive.ReactorExtension.reactor
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.Test

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

}