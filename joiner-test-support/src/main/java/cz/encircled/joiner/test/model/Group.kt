package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_group")
class Group : AbstractEntity() {

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    var users: Set<User>? = null

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    var statuses: Set<Status>? = null
}
