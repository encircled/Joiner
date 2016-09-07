package cz.encircled.joiner.test.model;

import cz.encircled.joiner.eclipse.InheritanceJoiningCustomizer;
import org.eclipse.persistence.annotations.Customizer;

import javax.persistence.*;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_password")
@Customizer(InheritanceJoiningCustomizer.class)
public class Password extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "normal_user_id")
    private NormalUser normalUser;

    public NormalUser getNormalUser() {
        return normalUser;
    }

    public void setNormalUser(NormalUser normalUser) {
        this.normalUser = normalUser;
    }
}
