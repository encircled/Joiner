package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * @author Kisel on 26.01.2016.
 */
@Entity
@Table(name = "test_status")
class Status : AbstractEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    var address: Address? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    var group: Group? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_type_id")
    var statusType: StatusType? = null
}
