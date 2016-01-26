package cz.encircled.joiner.test;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;

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
public class JoinTest extends AbstractTest {

    @Test
    public void noFetchJoinTest() {
        List<User> users = userRepository.find(Q.from(QUser.user));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        JoinDescription e = new JoinDescription(QUser.user.groups).fetch(false);

        users = userRepository.find(Q.from(QUser.user).joins(Collections.singletonList(e)));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        e.fetch(true);
        users = userRepository.find(Q.from(QUser.user).joins(Collections.singletonList(e)));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testNestedCollectionAndSingleJoin() {
        List<Address> addresses = addressRepository.find(Q.from(QAddress.address));

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));

        addresses = addressRepository.find(Q.from(QAddress.address)
                .addJoin(new JoinDescription(QAddress.address.user))
                .addJoin(new JoinDescription(QUser.user.groups)));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));
    }


    @Test
    public void testNestedCollectionJoin() {
        List<Group> groups = groupRepository.find(Q.from(QGroup.group));

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().get(0), "addresses"));

        groups = groupRepository.find(Q.from(QGroup.group)
                .addJoin(new JoinDescription(QGroup.group.users))
                .addJoin(new JoinDescription(QUser.user.addresses)));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().get(0), "addresses"));
    }

}
