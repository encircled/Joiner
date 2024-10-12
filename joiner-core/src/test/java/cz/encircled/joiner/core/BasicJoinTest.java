package cz.encircled.joiner.core;

import com.querydsl.core.JoinType;
import com.querydsl.core.Tuple;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.*;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TODO cleanup
 *
 * @author Kisel on 21.01.2016.
 */
public abstract class BasicJoinTest extends AbstractTest {

    public static final String USER_NO_ADDRESS = "user3";

    @Test
    public void testNestedJoinAlias() {
        JoinerQuery<Group, Group> q = Q.from(QGroup.group).joins(J.left(QUser.user1)
                .nested(J.left(QAddress.address).nested(QStatus.status)));
        List<JoinDescription> j = J.unrollChildrenJoins(q.getAllJoins().values());
        assertEquals(3, j.size());
        assertEquals("status_on_address_on_user1", j.get(j.size() - 1).getAlias().toString());
    }

    @Test
    public void testNestedJoinAliasWithNestedParentPath() {
        JoinerQuery<Group, Group> q = Q.from(QGroup.group).joins(J.left(QUser.user1)
                .nested(J.left(QAddress.address).nested(QAddress.address.statuses)));
        List<JoinDescription> j = J.unrollChildrenJoins(q.getAllJoins().values());
        assertEquals(3, j.size());
        assertEquals("status_on_address_on_user1", j.get(j.size() - 1).getAlias().toString());
    }

    @Test
    public void testNestedJoinAliasWithAllPathsViaParents() {
        JoinerQuery<Group, Group> q = Q.from(QGroup.group).joins(J.left(QGroup.group.users)
                .nested(J.left(QUser.user1.addresses).nested(QAddress.address.statuses)));
        List<JoinDescription> j = J.unrollChildrenJoins(q.getAllJoins().values());
        assertEquals(3, j.size());
        assertEquals("status_on_address_on_user1", j.get(j.size() - 1).getAlias().toString());
    }

    @Test
    public void testRightJoinSingleAssociation() {
        Assumptions.assumeFalse(isEclipse());
        List<User> users = joiner.find(Q.from(QUser.user1).joins(new JoinDescription(QGroup.group).right().fetch(false)));

        assertFalse(users.isEmpty());
        assertFalse(isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testInnerJoinSingleAssociation() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.inner(QGroup.group)));

        assertFalse(users.isEmpty());
        assertTrue(isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testInnerJoinSingleAssociationViaParentPath() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.inner(QUser.user1.groups)));

        assertFalse(users.isEmpty());
        assertTrue(isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testLeftJoinSingleAssociationViaParentPath() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(QUser.user1.groups));

        assertFalse(users.isEmpty());
        assertTrue(isLoaded(users.get(0), "groups"));
    }

    @Test
    public void noFetchJoinTest() {
        List<User> users = joiner.find(Q.from(QUser.user1));
        assertFalse(isLoaded(users.get(0), "groups"));

        JoinDescription e = J.left(QGroup.group).fetch(false);

        users = joiner.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        assertFalse(isLoaded(users.get(0), "groups"));

        e.fetch(true);
        entityManager.clear();
        users = joiner.find(Q.from(QUser.user1).joins(Collections.singletonList(e)));
        assertTrue(isLoaded(users.get(0), "groups"));
    }

    @Test
    public void testNestedCollectionAndSingleJoin() {
        if (isHibernate()) {
            List<Address> addresses = joiner.find(Q.from(QAddress.address));
            assertFalse(isLoaded(addresses.get(0), "user"));
            assertFalse(isLoaded(addresses.get(0).getUser(), "groups"));
            entityManager.clear();
        }

        List<Address> addresses = joiner.find(Q.from(QAddress.address)
                .joins(J.left(QUser.user1).nested(J.left(QGroup.group))));

        assertTrue(isLoaded(addresses.get(0), "user"));
        assertTrue(isLoaded(addresses.get(0).getUser(), "groups"));
    }


    @Test
    public void testNestedCollectionJoin() {
        List<Group> groups = joiner.find(Q.from(QGroup.group));

        assertFalse(isLoaded(groups.get(0), "users"));
        assertFalse(isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));

        entityManager.clear();

        groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QUser.user1)
                        .nested(J.left(QAddress.address))));

        assertTrue(isLoaded(groups.get(0), "users"));
        assertTrue(isLoaded(groups.get(0).getUsers().iterator().next(), "addresses"));
    }

    @Test
    public void testInnerJoin() {
        JoinerQuery<User, User> q = Q.from(QUser.user1)
                .joins(J.inner(QAddress.address));

        assertFalse(joiner.find(q).isEmpty());

        q.where(QUser.user1.name.eq("user3"));
        assertTrue(joiner.find(q).isEmpty());
    }

    @Test
    public void testRightJoin() {
        JoinerQuery<Address, Tuple> q = Q.select(QUser.user1.name, QAddress.address.name)
                .from(QAddress.address)
                .joins(J.right(QUser.user1))
                .where(QUser.user1.name.in("user1", USER_NO_ADDRESS));

        if (isEclipse()) {
            assertThrows(JoinerException.class, () -> joiner.find(q), "Right join is not supported in EclipseLink!");
        } else {
            List<Tuple> tuples = joiner.find(q);
            assertEquals(3, tuples.size());

            assertEquals("user1", tuples.get(0).get(QUser.user1.name));
            assertEquals("user1street1", tuples.get(0).get(QAddress.address.name));

            assertEquals("user1", tuples.get(1).get(QUser.user1.name));
            assertEquals("user1street2", tuples.get(1).get(QAddress.address.name));

            assertEquals(USER_NO_ADDRESS, tuples.get(2).get(QUser.user1.name));
            assertNull(tuples.get(2).get(QAddress.address.name));
        }
    }

    @Test
    public void testFetchRightJoin() {
        JoinerQuery<Address, Address> q = Q.from(QAddress.address)
                .joins(J.right(QUser.user1))
                .where(QUser.user1.name.isNotNull());

        if (isEclipse()) {
            assertThrows(JoinerException.class, () -> joiner.find(q), "Right join is not supported in EclipseLink!");
        } else {
            List<Address> addresses = joiner.find(q).stream().filter(Objects::nonNull).toList();

            assertFalse(addresses.isEmpty());
            for (Address address : addresses) {
                assertTrue(isLoaded(address, "user"));
            }
        }
    }

    @Test
    public void nonCollisionAliasCollectionJoinTest() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QStatus.status)));
    }

    @Test
    public void testRightJoinNoFetch() {
        if (isHibernate()) {
            List<Group> groups = joiner.find(Q.from(QGroup.group)
                    .joins(J.left(QUser.user1).right().fetch(false))
                    .where(QUser.user1.name.eq("user1")));
            assertFalse(groups.isEmpty());
        }
    }

    @Test
    public void testRightJoinNoFetchEclipse() {
        assertThrows(JoinerException.class, () -> {
            if (isEclipse()) {
                List<Group> groups = joiner.find(Q.from(QGroup.group)
                        .joins(J.left(QUser.user1).right()
                                .fetch(false))
                        .where(QUser.user1.name.eq("user1")));
                assertFalse(groups.isEmpty());
            } else {
                throw new JoinerException("Test");
            }
        });
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

    @Test
    public void testDefaultJoinFromEntityPath() {
        JoinerQuery<Group, Group> query = Q.from(QGroup.group).joins(QUser.user1);

        JoinDescription join = query.getJoins().iterator().next();
        assertEquals(JoinType.LEFTJOIN, join.getJoinType());
        assertEquals(QUser.user1, join.getAlias());
    }

    @Test
    public void testDefaultNestedJoinFromEntityPath() {
        JoinerQuery<Group, Group> query = Q.from(QGroup.group).joins(J.left(QUser.user1).nested(QStatus.status));

        JoinDescription join = query.getJoins().iterator().next().getChildren().iterator().next();
        assertEquals(JoinType.LEFTJOIN, join.getJoinType());
        assertEquals(J.path(QUser.user1, QStatus.status), join.getAlias());
    }

    @Test
    public void collisionAliasCollectionJoinTest() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QStatus.status), J.left(QUser.user1).nested(J.left(QStatus.status))));
    }

    @Test
    public void testJoinUsingParentPath() {
        JoinerQuery<User, User> query = Q.from(QUser.user1).joins(QUser.user1.groups, QUser.user1.statuses);

        assertNotNull(query.getJoin(QStatus.status));
        assertNotNull(query.getJoin(QGroup.group));
        assertEquals(2, query.getJoins().size());
        assertEquals(2, J.unrollChildrenJoins(query.getJoins()).size());
    }

    @Test
    public void testInnerJoinManyToOne() {
        List<Password> passwords = joiner.find(Q.from(QPassword.password).joins(J.inner(QNormalUser.normalUser)));

        assertFalse(passwords.isEmpty());

        for (Password password : passwords) {
            assertTrue(isLoaded(password, "normalUser"));
        }
    }

    @Test
    public void testLeftJoinManyToOne() {
        List<Password> passwords = joiner.find(Q.from(QPassword.password).joins(QNormalUser.normalUser));

        assertFalse(passwords.isEmpty());

        for (Password password : passwords) {
            assertTrue(isLoaded(password, "normalUser"));
        }
    }

    @Test
    public void testQueryGetJoin() {
        JoinerQuery<User, User> query = Q.from(QUser.user1).addHint("", null);

        assertNull(query.getJoin(QGroup.group));
        query.joins(QGroup.group);
        assertNotNull(query.getJoin(QGroup.group));
    }

    @Test
    public void testGetNestedJoin() {
        JoinerQuery<User, User> query = Q.from(QUser.user1)
                .joins(J.left(QGroup.group))
                .addHint("", null);

        assertNull(query.getJoin(QGroup.group).getJoin(QStatus.status));
        query.getJoin(QGroup.group).nested(QStatus.status);
        assertNotNull(query.getJoin(QGroup.group).getJoin(QStatus.status));
    }

    @Test
    public void testJoinSelfReference() {
        WithSelfReference result = joiner.findOne(Q.from(QWithSelfReference.withSelfReference)
                .where(QWithSelfReference.withSelfReference.name.eq("refWithParent"))
                .joins(new QWithSelfReference("self")));
        assertTrue(isLoaded(result.selfReference, "normalUser"));
    }

    @Test
    public void testPredicateAliasInWhere() {
        JoinerQuery<Group, Group> query = Q.from(QGroup.group)
                .joins(J.left(new QStatus("groupStatus")), J.left(QUser.user1).nested(new QStatus("userStatus")))
                .where(new QStatus("userStatus").id.isNotNull());
        assertQueryContains("userStatus_on_user1.id is not null", query);
    }

    @Test
    public void testPredicateAliasInJoinOn() {
        JoinerQuery<Group, Group> query = Q.from(QGroup.group)
                .joins(J.left(new QStatus("groupStatus")), J.left(QUser.user1).nested(
                        J.left(new QStatus("userStatus")).on(new QStatus("userStatus").id.isNotNull())
                ));
        assertQueryContains("userStatus_on_user1.id is not null", query);
    }

}
