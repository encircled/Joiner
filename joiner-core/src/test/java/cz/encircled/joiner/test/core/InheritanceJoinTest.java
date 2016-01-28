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
                .addJoin(J.join(QGroup.group.users).alias(QSuperUser.superUser._super))
                .addJoin(J.join(QSuperUser.superUser.key)));

        check(groups, true, false);
    }

    @Test
    public void joinSingleAndCollectionMultipleChildrenTest() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.users).alias(QSuperUser.superUser._super))
                .addJoin(J.join(QGroup.group.users).alias(QNormalUser.normalUser._super))
                .addJoin(J.join(QSuperUser.superUser.key))
                .addJoin(J.join(QNormalUser.normalUser.passwords))
        );

        check(groups, true, true);
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
                    if (hasKey) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "key"));
                    }
                    hasKey = true;
                }
                if (user instanceof NormalUser) {
                    if (hasPassword) {
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
