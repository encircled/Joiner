package cz.encircled.joiner.springbootexample

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    var customer: Customer? = null
) : AbstractEntity()

@Entity
@Table
class Customer(
    @Column
    var name: String = "",

    @OneToMany(mappedBy = "customer")
    val employments: List<Employment> = listOf()
) : AbstractEntity()