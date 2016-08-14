package cz.encircled.joiner.test.model;

import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

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
    @BatchFetch(BatchFetchType.JOIN)
    private Set<Password> passwords;

    public Set<Password> getPasswords() {
        return passwords;
    }

    public void setPasswords(Set<Password> passwords) {
        this.passwords = passwords;
    }
}
