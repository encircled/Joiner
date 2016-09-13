package cz.encircled.joiner.test.core;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QKey;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QSuperUser;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.SuperUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kisel on 21.01.2016.
 */
public class BasicJoinTest extends AbstractTest {

    @Test
    public void noFetchJoinTest() {
        List<User> users = joiner.find(Q.from(QUser.user1));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        JoinDescription e = J.left(QGroup.group).fetch(false);

        users = joiner.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        e.fetch(true);
        entityManager.clear();
        users = joiner.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testFoundSubclassPredicated() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(J.left(QKey.key)))
                .joins(J.left(QStatus.status))
                .where(QSuperUser.superUser.key.name.eq("key1"))
        );

        Assert.assertFalse(groups.isEmpty());

        for (Group group : groups) {
            boolean hasKey = false;
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    SuperUser superUser = (SuperUser) user;
                    if (superUser.getKey() != null && superUser.getKey().getName().equals("key1")) {
                        hasKey = true;
                    }
                }
            }
            Assert.assertTrue(hasKey);
        }
    }

    @Test
    public void testNotFoundSubclassPredicated() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                                .nested(J.left(QKey.key)),
                        J.left(QStatus.status))
                .where(J.path(QSuperUser.superUser, QKey.key).name.eq("not_exists"))
        );

        Assert.assertTrue(groups.isEmpty());
    }

    @Test
    public void testNestedCollectionAndSingleJoin() {
        if (noProfiles("eclipse")) {
            List<Address> addresses = joiner.find(Q.from(QAddress.address));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));
            entityManager.clear();
        }

        List<Address> addresses = joiner.find(Q.from(QAddress.address)
                .joins(J.left(QUser.user1).nested(J.left(QGroup.group))));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0), "user"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(addresses.get(0).getUser(), "groups"));
    }


    @Test
    public void testNestedCollectionJoin() {
        List<Group> groups = joiner.find(Q.from(QGroup.group));

        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));

        entityManager.clear();

        groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1)
                        .nested(J.left(QAddress.address))));

        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0), "users"));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));
    }

    @Test
    public void testInnerJoin() {
        JoinerQuery<User, User> q = Q.from(QUser.user1)
                .joins(J.inner(QAddress.address));

        Assert.assertFalse(joiner.find(q).isEmpty());

        q.where(QUser.user1.name.eq("user3"));
        Assert.assertTrue(joiner.find(q).isEmpty());
    }

    @Test
    public void nonCollisionAliasCollectionJoinTest() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QStatus.status)));
    }

    @Test
    public void testRightJoinNoFetch() {
        if (noProfiles("eclipse")) {
            List<Group> groups = joiner.find(Q.from(QGroup.group)
                    .joins(J.left(QUser.user1).right().fetch(false))
                    .where(QUser.user1.name.eq("user1")));
            Assert.assertFalse(groups.isEmpty());
        }
    }

    @Test(expected = JoinerException.class)
    public void testRightJoinNoFetchEclipse() {
        if (isEclipse()) {
            List<Group> groups = joiner.find(Q.from(QGroup.group)
                    .joins(J.left(QUser.user1).right()
                            .fetch(false))
                    .where(QUser.user1.name.eq("user1")));
            Assert.assertFalse(groups.isEmpty());
        } else {
            throw new JoinerException("Test");
        }
    }

    @Test
    public void testNonDistinct() {
        int nonDistinct = joiner.find(Q.from(QUser.user1)
                .joins(J.left(QAddress.address)
                        .nested(J.left(QStatus.status)))
                .distinct(false))
                .size();

        entityManager.clear();

        int distinct = joiner.find(Q.from(QUser.user1).joins(J.left(QAddress.address))).size();

        if (isEclipse()) {
            Assert.assertTrue(distinct == nonDistinct);
        } else {
            Assert.assertTrue(distinct < nonDistinct);
        }
    }

    @Test
    public void testJoinOn() {
        String name = "user1";

        List<User> groups = joiner.find(
                Q.select(QUser.user1)
                        .from(QGroup.group)
                        .joins(J.inner(QUser.user1)
                                .on(QUser.user1.name.eq(name))
                                .fetch(false))
        );
        assertHasName(groups, name);
    }

}
