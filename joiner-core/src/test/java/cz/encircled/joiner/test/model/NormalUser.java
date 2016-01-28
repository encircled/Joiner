package cz.encircled.joiner.test.model;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_normal_user")
@DiscriminatorValue("normal_user")
public class NormalUser extends User {

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "normalUser")
    private Set<Password> passwords;

    public Set<Password> getPasswords() {
        return passwords;
    }

    public void setPasswords(Set<Password> passwords) {
        this.passwords = passwords;
    }
}
