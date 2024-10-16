package cz.encircled.joiner.springwebfluxexample.controller

import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.countOf
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.kotlin.reactive.KtReactiveJoiner
import cz.encircled.joiner.springwebfluxexample.Employment
import cz.encircled.joiner.springwebfluxexample.QEmployment
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/coroutines/employment")
class KtCoroutinesEmploymentController {

    @Autowired
    lateinit var joiner: KtReactiveJoiner

    @PostConstruct
    fun init() = runBlocking{
        // Insert test data
        /*if (joiner.findOne(QEmployment.employment.countOf()) == 0L) {
            joiner.persist(
                listOf(
                    Employment().apply { name = "Employment 1" },
                    Employment().apply { name = "Employment 2" },
                    Employment().apply { name = "Employment 3" },
                )
            )
        }*/
    }

    @GetMapping("{id}")
    suspend fun getOne(@PathVariable id: Long): Employment {
        return joiner.findOne(QEmployment.employment.all() where { it.id eq id })
    }

    @GetMapping
    suspend fun getAll(): List<Employment> {
        return joiner.find(QEmployment.employment.all())
    }

    @GetMapping("/names")
    suspend fun getNames(): List<String> {
        return joiner.find(QEmployment.employment.name from QEmployment.employment)
    }

}