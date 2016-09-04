package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_password")
class Password : AbstractEntity() {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "normal_user_id")
    var normalUser: NormalUser? = null
}
