package cz.encircled.joiner.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_user")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn
@DiscriminatorValue("user")
public class User extends AbstractEntity {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Address> addresses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Contact> contacts;

    @Transient
    private Set<Phone> phones;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "employmentUser")
    private Set<Contact> employmentContacts;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private Set<Status> statuses;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_to_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private List<Group> groups = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "parent_id")
    private User user;

    public Set<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    public Set<Contact> getEmploymentContacts() {
        return employmentContacts;
    }

    public void setEmploymentContacts(Set<Contact> employmentContacts) {
        this.employmentContacts = employmentContacts;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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
