package cz.encircled.joiner.test.model;

import javax.persistence.*;

/**
 * @author Kisel on 21.01.2016.
 */
@MappedSuperclass
public class AbstractEntity {

    @Column
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "id_generator")
    @SequenceGenerator(name = "id_generator", sequenceName = "id_seq", initialValue = 1, allocationSize = 1)
    protected Long id;

    @Column
    protected String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }
}
