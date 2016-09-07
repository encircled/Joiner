package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * @author Kisel on 27.01.2016.
 */
@Entity
@Table(name = "status_type")
@Customizer(InheritanceJoiningCustomizer.class)
public class StatusType extends AbstractEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "statusType")
    private Set<Status> statuses;

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<Status> statuses) {
        this.statuses = statuses;
    }
}
