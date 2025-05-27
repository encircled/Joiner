package cz.encircled.joiner.springbootexample.controller

import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.springbootexample.Employment
import cz.encircled.joiner.springbootexample.EmploymentDto
import cz.encircled.joiner.springbootexample.QEmployment.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/employment")
class EmploymentController {

    @Autowired
    lateinit var joiner: JoinerKt

    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long): EmploymentDto {
        return joiner.getOne(employment.all() where { it.id eq id }).toDto()
    }

    @GetMapping
    fun getAll(): List<EmploymentDto> {
        return joiner.find(employment.all()).map { it.toDto() }
    }

    @GetMapping("/names")
    fun getNames(): List<String> {
        return joiner.find(employment.name from employment)
    }

}