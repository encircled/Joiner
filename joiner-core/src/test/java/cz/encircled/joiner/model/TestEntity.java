package cz.encircled.joiner.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author Kisel on 21.01.2016.
 */
@MappedSuperclass
public class TestEntity {

    @Id
    @Column(name = "id")
    protected Long id;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

}
