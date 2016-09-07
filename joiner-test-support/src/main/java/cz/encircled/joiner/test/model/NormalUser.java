package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.BatchFetch;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_normal_user")
@DiscriminatorValue("normal_user")
@Customizer(InheritanceJoiningCustomizer.class)
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
