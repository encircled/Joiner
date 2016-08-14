package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.Persistence;
import java.util.List;

/**
 * Created by Kisel on 28.01.2016.
 */
public class InheritanceJoinTest extends AbstractTest {

    @Before
    public void before() {
        Assume.assumeTrue(noProfiles("eclipse"));
    }

    @Test
    public void joinSingleEntityOnChildTest() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).alias(new QUser("superUser")), J.left(QSuperUser.superUser.key)));

        check(groups, true, false);
    }

    @Test
    public void joinSingleAndCollectionMultipleChildrenTest() {
        List<Group> groups = joiner.find(new Q<Group>()
                .joins(J.left(QUser.user1)
                        .nested(J.left(QKey.key), J.left(QPassword.password)))
                .where(QKey.key.name.ne("bad_key"))
        );

        check(groups, true, false);
    }

    @Test
    public void joinCollectionOnChildTest() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1).alias(QNormalUser.normalUser._super).nested(J.left(QPassword.password)))
        );

        check(groups, false, true);
    }

    @Test
    public void nestedTest() {
        List<Address> addresses = joiner.find(Q.from(QAddress.address)
                .joins(J.left(QUser.user1).nested(J.left(QPassword.password)))
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
