package cz.encircled.joiner.springwebfluxexample.controller

import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.reactive.ReactorJoiner
import cz.encircled.joiner.springwebfluxexample.Employment
import cz.encircled.joiner.springwebfluxexample.QEmployment
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono


@RestController
@RequestMapping("/employment")
class ReactorEmploymentController {

    @Autowired
    lateinit var joiner: ReactorJoiner

    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long): Mono<Employment> {
        return joiner.findOne(QEmployment.employment.all() where { it.id eq id })
            .onErrorResume { Mono.just(Employment()) }
    }

    @GetMapping
    fun getAll(): Flux<Employment> {
        return joiner.find(QEmployment.employment.all())
    }

    @GetMapping("/names")
    fun getNames(): Flux<String> {
        return joiner.find(QEmployment.employment.name from QEmployment.employment)
    }

}