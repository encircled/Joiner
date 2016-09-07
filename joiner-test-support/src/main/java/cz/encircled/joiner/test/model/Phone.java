package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.*;
import java.util.Set;

/**
 * @author Vlad on 21-Aug-16.
 */
@Entity
@DiscriminatorValue("phone")
@Customizer(InheritanceJoiningCustomizer.class)
public class Phone extends Contact {

    @Column(name = "number")
    private String number;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "phone")
    private Set<Status> statuses;

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(Set<Status> statuses) {
        this.statuses = statuses;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

}
