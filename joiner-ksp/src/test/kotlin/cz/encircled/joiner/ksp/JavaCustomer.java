package cz.encircled.joiner.ksp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class JavaCustomer {

    @Column
    String[] arrayOfStrings;

}
