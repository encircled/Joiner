package cz.encircled.joiner.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;

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
