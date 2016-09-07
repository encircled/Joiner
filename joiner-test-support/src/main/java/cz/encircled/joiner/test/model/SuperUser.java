package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.*;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_super_user")
@DiscriminatorValue("super_user")
@Customizer(InheritanceJoiningCustomizer.class)
public class SuperUser extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id")
    private Key key;

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }

}
