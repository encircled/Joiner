package cz.encircled.joiner;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.model.Group;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.GroupRepository;
import cz.encircled.joiner.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kisel on 21.01.2016.
 */
public class JoinTest extends AbstractTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Test
    public void listJoinTest() {
        prepareData();

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
    public void testNestedJoin() {
        prepareData();

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
