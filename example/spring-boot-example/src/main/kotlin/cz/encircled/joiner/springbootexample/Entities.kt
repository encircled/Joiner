package cz.encircled.joiner.springbootexample

import jakarta.persistence.*

@MappedSuperclass
abstract class AbstractEntity {
    @Id
    var id: Long? = null
}

@Entity
@Table
class Employment(
    @Column
    var name: String = "",

    @Column
    var salary: Double? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    var person: Person? = null
) : AbstractEntity() {
    fun toDto() = EmploymentDto(id!!, name, person?.name)
}

@Entity
@Table
class Person(
    @Column
    var name: String = "",

    @OneToMany(mappedBy = "person", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val employments: MutableList<Employment> = mutableListOf()
) : AbstractEntity() {
    fun toDto() = CustomerDto(id!!, name, employments.map { it.toDto() })
}

data class EmploymentDto(val id: Long, val name: String, val customer: String?)

data class CustomerDto(val id: Long, val name: String, val employments: List<EmploymentDto>)