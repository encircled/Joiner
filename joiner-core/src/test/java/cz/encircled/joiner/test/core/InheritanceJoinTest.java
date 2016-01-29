package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Persistence;
import java.util.List;

/**
 * Created by Kisel on 28.01.2016.
 */
public class InheritanceJoinTest extends AbstractTest {

    @Test
    public void joinSingleEntityOnChildTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.users).alias(new QUser("superUser")))
                .join(J.join(QSuperUser.superUser.key))
        );

        check(groups, true, false);
    }

    @Test
    public void joinSingleAndCollectionMultipleChildrenTest() {
        List<Group> groups = groupRepository.find(new Q<Group>()
                .joins(QGroup.group.users, QSuperUser.superUser.key, QNormalUser.normalUser.passwords)
                .where(QKey.key.name.ne("bad_key"))
        );

        check(groups, true, false);
    }

    @Test
    public void joinCollectionOnChildTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.users).alias(QNormalUser.normalUser._super))
                .join(QNormalUser.normalUser.passwords)
        );

        check(groups, false, true);
    }

    @Test
    public void nestedTest() {
        List<Address> addresses = addressRepository.find(Q.from(QAddress.address)
                .join(QAddress.address.user)
                .join(J.join(QNormalUser.normalUser.passwords).inner())
        );

        Assert.assertFalse(addresses.isEmpty());
        for (Address address : addresses) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(address, "user"));
            Assert.assertTrue(address.getUser() instanceof NormalUser);

            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(address.getUser(), "passwords"));
        }
    }

    private void check(List<Group> groups, boolean key, boolean password) {
        boolean hasKey = false;
        boolean hasPassword = false;

        for (Group group : groups) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    if (key) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "key"));
                    }
                    hasKey = true;
                }
                if (user instanceof NormalUser) {
                    if (password) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
                    }
                    hasPassword = true;
                }
            }
        }

        if (key) {
            Assert.assertTrue(hasKey);
        }
        if (password) {
            Assert.assertTrue(hasPassword);
        }
    }

}
