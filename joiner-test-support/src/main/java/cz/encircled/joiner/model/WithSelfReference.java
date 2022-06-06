package cz.encircled.joiner.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "self_reference")
public class WithSelfReference extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "reference_id")
    public WithSelfReference selfReference;

}
