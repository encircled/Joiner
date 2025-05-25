package cz.encircled.joiner.ksp

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
class Employment() : AbstractEntity() {

    @Column
    var name: String? = null

    @ManyToOne
    @JoinColumn(name = "customer_id")
    var customer: Customer? = null

    constructor(name: String?) : this() {
        this.name = name
    }
}

@Entity
@Table
class Customer() : AbstractEntity() {

    @OneToMany(mappedBy = "customer")
    var employments: List<Employment> = listOf()

}