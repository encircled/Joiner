package cz.encircled.joiner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "self_reference")
public class WithSelfReference extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "reference_id")
    public WithSelfReference selfReference;

}
