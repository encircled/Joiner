package cz.encircled.joiner.test.model;

import javax.persistence.*;

/**
 * @author Kisel on 26.01.2016.
 */
@Entity
@Table(name = "test_status")
public class Status extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_id")
    private Phone phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_type_id")
    private StatusType statusType;

    public StatusType getStatusType() {
        return statusType;
    }

    public void setStatusType(final StatusType statusType) {
        this.statusType = statusType;
    }

    public Phone getPhone() {
        return phone;
    }

    public void setPhone(Phone phone) {
        this.phone = phone;
    }

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(final Address address) {
        this.address = address;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(final Group group) {
        this.group = group;
    }
}
