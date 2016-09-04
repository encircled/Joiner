package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * @author Vlad on 21-Aug-16.
 */
@Table(name = "contact")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
open class Contact : AbstractEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "e_user_id")
    var employmentUser: User? = null

}
