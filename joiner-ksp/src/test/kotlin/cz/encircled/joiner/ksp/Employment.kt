package cz.encircled.joiner.ksp

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

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

    var doubleValue: Double? = null
    var booleanValue: Boolean? = null
    var localDateValue: LocalDate? = null
    var floatValue: Float? = null
    var intValue: Int? = null
    var stringValue: String? = null
    var localDateTimeValue: LocalDateTime? = null
    var bigDecimalValue: BigDecimal? = null
    var byteValue: Byte? = null
    var shortValue: Short? = null

}
