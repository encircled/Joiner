package cz.encircled.joiner.model;

import jakarta.persistence.*;

/**
 * @author Vlad on 21-Aug-16.
 */
@Table(name = "contact")
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn
public class Contact extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "e_user_id")
    private User employmentUser;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getEmploymentUser() {
        return employmentUser;
    }

    public void setEmploymentUser(User employmentUser) {
        this.employmentUser = employmentUser;
    }

}
