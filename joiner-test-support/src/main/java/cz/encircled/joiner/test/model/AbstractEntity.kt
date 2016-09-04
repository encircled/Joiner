package cz.encircled.joiner.test.model

import javax.persistence.*

/**
 * @author Kisel on 21.01.2016.
 */
@MappedSuperclass
open class AbstractEntity {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_generator")
    @SequenceGenerator(name = "id_generator", sequenceName = "id_seq", initialValue = 1, allocationSize = 1)
    var id: Long? = null

    @Column
    var name: String? = null

}
