package cz.encircled.joiner.test.model

import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table

/**
 * @author Kisel on 27.01.2016.
 */
@Entity
@Table(name = "status_type")
class StatusType : AbstractEntity() {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "statusType")
    var statuses: Set<Status>? = null
}
