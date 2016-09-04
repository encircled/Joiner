package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * @author Kisel on 25.01.2016.
 */
@Entity
@Table(name = "test_address")
class Address : AbstractEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "address")
    var statuses: Set<Status>? = null
}
