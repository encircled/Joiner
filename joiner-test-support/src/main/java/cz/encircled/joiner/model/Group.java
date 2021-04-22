package cz.encircled.joiner.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_group")
public class Group extends AbstractEntity {

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private Set<User> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "group")
    private Set<Status> statuses;

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<Status> statuses) {
        this.statuses = statuses;
    }
}
