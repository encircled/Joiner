package cz.encircled.joiner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Created by Kisel on 28.01.2016.
 */
@Entity
@Table(name = "test_password")
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
