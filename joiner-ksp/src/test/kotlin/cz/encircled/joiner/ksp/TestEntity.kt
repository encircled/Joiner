package cz.encircled.joiner.ksp

import com.querydsl.core.annotations.QueryTransient
import jakarta.persistence.*

/**
 * A simple test entity for testing the KSP processor.
 */
@Entity
@Table(name = "test_entity")
class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "active")
    var active: Boolean = false

    @OneToMany(mappedBy = "parent")
    var children: Set<TestEntity>? = null

    @ManyToOne
    @JoinColumn(name = "parent_id")
    var parent: TestEntity? = null

    @ManyToOne
    val testEntity: TestEntity? = null // self ref with the same name

    @Transient
    var transientField: String? = null

    @QueryTransient
    var transientField2: String? = null

    @get:Transient
    val transientFieldWithCustomGetter get() = "Custom Getter"

}