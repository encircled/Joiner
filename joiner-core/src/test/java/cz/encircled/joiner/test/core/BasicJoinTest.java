package cz.encircled.joiner.test.core;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kisel on 21.01.2016.
 */
public class BasicJoinTest extends AbstractTest {

    @Test
    public void noFetchJoinTest() {
        List<User> users = userRepository.find(Q.from(QUser.user1));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        JoinDescription e = J.join(QUser.user1.groups).fetch(false);

        users = userRepository.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        e.fetch(true);
        entityManager.clear();
        users = userRepository.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testNestedCollectionAndSingleJoin() {
        if (noProfiles("eclipse")) {
            List<Address> addresses = addressRepository.find(Q.from(QAddress.address));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));
            entityManager.clear();
        }

        List<Address> addresses = addressRepository.find(Q.from(QAddress.address)
                .join(J.join(QAddress.address.user))
                .join(J.join(QUser.user1.groups)));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));
    }


    @Test
    public void testNestedCollectionJoin() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group));

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));

        entityManager.clear();

        groups = groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.users))
                .join(J.join(QUser.user1.addresses)));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));
    }

    @Test
    public void testInnerJoin() {
        Q<User> q = Q.from(QUser.user1)
                .join(J.join(QUser.user1.addresses).inner());

        Assert.assertFalse(userRepository.find(q).isEmpty());

        q.where(QUser.user1.name.eq("user3"));
        Assert.assertTrue(userRepository.find(q).isEmpty());
    }

    @Test
    public void nonCollisionAliasCollectionJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.statuses)));
    }

    @Test
    public void testRightJoinNoFetch() {
        if (noProfiles("eclipse")) {
            List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                    .join(J.join(QGroup.group.users).right().fetch(false))
                    .where(QUser.user1.name.eq("user1")));
            Assert.assertFalse(groups.isEmpty());
        }
    }

    @Test(expected = JoinerException.class)
    public void testRightJoinNoFetchEclipse() {
        if (isEclipse()) {
            List<Group> groups = groupRepository.find(Q.from(QGroup.group)
                    .join(J.join(QGroup.group.users).right().fetch(false))
                    .where(QUser.user1.name.eq("user1")));
            Assert.assertFalse(groups.isEmpty());
        } else {
            throw new JoinerException("Test");
        }
    }

    @Test
    public void testNonDistinct() {
        int nonDistinct = userRepository.find(Q.from(QUser.user1)
                .joins(QUser.user1.addresses, QAddress.address.statuses)
                .distinct(false)).size();
        entityManager.clear();
        int distinct = userRepository.find(Q.from(QUser.user1).join(J.join(QUser.user1.addresses))).size();

        if (isEclipse()) {
            Assert.assertTrue(distinct == nonDistinct);
        } else {
            Assert.assertTrue(distinct < nonDistinct);
        }
    }

    @Test
    public void testJoinOn() {
        String name = "user1";

        List<User> groups = groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.users).inner().on(QUser.user1.name.eq(name)).fetch(false)), QUser.user1
        );
        assertHasName(groups, name);
    }

}
