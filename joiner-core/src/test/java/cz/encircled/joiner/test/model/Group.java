package cz.encircled.joiner.test.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * @author Kisel on 21.01.2016.
 */
@Entity
@Table(name = "test_group")
public class Group extends AbstractEntity {

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "groups")
    private List<User> users = new ArrayList<User>();

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(final List<User> users) {
        this.users = users;
    }
}
