package cz.encircled.joiner.test.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * @author Vlad on 21-Aug-16.
 */
@Entity
@DiscriminatorValue("phone")
public class Phone extends Contact {

    @Column(name = "number")
    private String number;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
