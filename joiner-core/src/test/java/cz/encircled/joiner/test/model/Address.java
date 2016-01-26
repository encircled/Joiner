package cz.encircled.joiner.test.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Kisel on 25.01.2016.
 */
@Entity
@Table(name = "test_address")
public class Address extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = false)
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "address")
    private Set<Status> statuses;

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<Status> statuses) {
        this.statuses = statuses;
    }
}
