package cz.encircled.joiner.springbootexample.controller

import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.springbootexample.Employment
import cz.encircled.joiner.springbootexample.QEmployment.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/kt/employment")
class KtEmploymentController {

    @Autowired
    lateinit var joiner: JoinerKt

    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long): Employment {
        return joiner.getOne(employment.all() where { it.id eq id })
    }

    @GetMapping
    fun getAll(): List<Employment> {
        return joiner.find(employment.all())
    }

    @GetMapping("/names")
    fun getNames(): List<String> {
        return joiner.find(employment.name from employment)
    }

}