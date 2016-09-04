package cz.encircled.joiner.test.model

import org.eclipse.persistence.annotations.BatchFetch
import org.eclipse.persistence.annotations.BatchFetchType

import javax.persistence.*

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_normal_user")
@DiscriminatorValue("normal_user")
class NormalUser : User() {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "normalUser")
    @BatchFetch(BatchFetchType.JOIN)
    var passwords: Set<Password>? = null
}
