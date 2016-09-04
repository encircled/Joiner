package cz.encircled.joiner.test.model

import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

/**
 * @author Vlad on 21-Aug-16.
 */
@Entity
@DiscriminatorValue("phone")
class Phone : Contact() {

    @Column(name = "number")
    var number: String? = null

}
