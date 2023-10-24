package cz.encircled.joiner.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_super_user")
@DiscriminatorValue("super_user")
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
