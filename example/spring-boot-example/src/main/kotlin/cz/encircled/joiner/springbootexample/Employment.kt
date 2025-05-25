package cz.encircled.joiner.springbootexample;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table
public class Employment {

    @Id
    public Long id;

    @Column
    public String name;

    public Employment() {
    }

    public Employment(String name) {
        this.id = 1L;
        this.name = name;
    }
}
