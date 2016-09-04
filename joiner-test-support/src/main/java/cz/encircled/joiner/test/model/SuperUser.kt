package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_super_user")
@DiscriminatorValue("super_user")
class SuperUser : User() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id")
    var key: Key? = null

}
