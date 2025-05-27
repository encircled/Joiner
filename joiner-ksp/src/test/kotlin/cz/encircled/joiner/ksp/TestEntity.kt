package cz.encircled.joiner.ksp

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

    @Transient
    var transientField: String? = null
}