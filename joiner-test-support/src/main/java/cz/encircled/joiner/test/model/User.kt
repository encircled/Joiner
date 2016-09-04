package cz.encircled.joiner.test.model

import java.util.*
import javax.persistence.*

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
@DiscriminatorValue("user")
open class User : AbstractEntity() {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var addresses: Set<Address>? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var contacts: Set<Contact>? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employmentUser")
    var employmentContacts: Set<Contact>? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    var statuses: Set<Status>? = null

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_to_group", joinColumns = arrayOf(JoinColumn(name = "user_id")), inverseJoinColumns = arrayOf(JoinColumn(name = "group_id")))
    var groups: List<Group> = ArrayList()

    @OneToOne
    @JoinColumn(name = "parent_id")
    var user: User? = null
}
