package cz.encircled.joiner.test.core;

import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.NormalUser;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QNormalUser;
import cz.encircled.joiner.test.model.QSuperUser;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.SuperUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Kisel on 28.01.2016.
 */
public class InheritanceJoinTest extends AbstractTest {

    @Test
    public void joinSingleEntityOnChildTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.users).alias(new QUser("superUser")))
                .addJoin(J.join(QSuperUser.superUser.key))
        );

        check(groups, true, false);
    }

    @Test
    public void joinSingleAndCollectionMultipleChildrenTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.users))
                .addJoin(J.join(QSuperUser.superUser.key))
                .addJoin(J.join(QNormalUser.normalUser.passwords))
        );

        check(groups, true, false);
    }

    @Test
    public void joinCollectionOnChildTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.users).alias(QNormalUser.normalUser._super))
                .addJoin(J.join(QNormalUser.normalUser.passwords))
        );

        check(groups, false, true);
    }

    @Test
    public void nestedTest() {
        List<Address> addresses = addressRepository.find(Q.from(QAddress.address)
                .addJoin(J.join(QAddress.address.user).alias(QNormalUser.normalUser._super))
                .addJoin(J.join(QNormalUser.normalUser.passwords))
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
