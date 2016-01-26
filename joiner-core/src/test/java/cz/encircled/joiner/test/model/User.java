package cz.encircled.joiner.test.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_user")
public class User extends AbstractEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Address> addresses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Status> statuses;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_to_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups = new ArrayList<Group>();

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(final List<Group> groups) {
        this.groups = groups;
    }

    public Set<Status> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<Status> statuses) {
        this.statuses = statuses;
    }
}
