package cz.encircled.joiner.springbootexample.controller

import cz.encircled.joiner.kotlin.JoinerKt
import cz.encircled.joiner.kotlin.JoinerKtOps.eq
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.all
import cz.encircled.joiner.kotlin.JoinerKtQueryBuilder.from
import cz.encircled.joiner.springbootexample.Customer
import cz.encircled.joiner.springbootexample.QCustomer.customer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/customer")
class CustomerController {

    @Autowired
    lateinit var joiner: JoinerKt

    @GetMapping("{id}")
    fun getOne(@PathVariable id: Long): CustomerDto {
        return joiner.getOne(customer.all() where { it.id eq id }).toDto()
    }

    @GetMapping
    fun getAll(): List<CustomerDto> {
        return joiner.find(customer.all()).map { it.toDto() }
    }

    @GetMapping("/names")
    fun getNames(): List<String> {
        return joiner.find(customer.name from customer)
    }

    data class CustomerDto(val id: Long, val name: String, val employments: List<String>)

    fun Customer.toDto() = CustomerDto(id!!, name!!, employments.map { it.name!! })

}