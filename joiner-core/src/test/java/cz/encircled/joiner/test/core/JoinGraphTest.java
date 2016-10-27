package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Vlad on 15-Aug-16.
 */
@Transactional
public class JoinGraphTest extends AbstractTest {

    private DefaultJoinGraphRegistry mockRegistry;

    @Before
    public void before() {
        mockRegistry = new DefaultJoinGraphRegistry();

        try {
            joinGraphRegistry.getJoinGraph(Group.class, "users");
        } catch (Exception e) {
            joinGraphRegistry.registerJoinGraph("statuses", Collections.singletonList(J.left(QStatus.status)), Group.class);
            joinGraphRegistry.registerJoinGraph("users", Collections.singletonList(J.left(QUser.user1)), Group.class);
            joinGraphRegistry.registerJoinGraph("userStatuses", Collections.singletonList(J.left(QUser.user1).nested(QStatus.status)), Group.class);
            joinGraphRegistry.registerJoinGraph("usersAddress", Collections.singletonList(J.left(QUser.user1).nested(QAddress.address)), Group.class);
            joinGraphRegistry.registerJoinGraph("addressStatuses", Collections.singletonList(J.left(QUser.user1)
                    .nested(J.left(QAddress.address).nested(QStatus.status))), Group.class);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullName() {
        mockRegistry.registerJoinGraph(null, Collections.singletonList(J.left(QUser.user1)), Group.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullJoins() {
        mockRegistry.registerJoinGraph("test", null, Group.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullClass() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmptyClasses() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)));
    }

    @Test(expected = JoinerException.class)
    public void testDuplicatedName() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)), Group.class);
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)), Group.class);
    }

    @Test(expected = JoinerException.class)
    public void testGraphMissing() {
        mockRegistry.getJoinGraph(Group.class, "NotExists");
    }

    @Test
    public void testDuplicatedNameForDifferentClasses() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)), Group.class);
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(QUser.user1)), User.class);
    }

    @Test
    public void testAddToRegistry() {
        List<JoinDescription> joins = Collections.singletonList(J.left(QUser.user1));
        List<JoinDescription> joins2 = Collections.singletonList(J.left(QStatus.status));

        mockRegistry.registerJoinGraph("users", joins, Group.class);
        mockRegistry.registerJoinGraph("statuses", joins2, Group.class);

        Assert.assertEquals(joins, mockRegistry.getJoinGraph(Group.class, "users"));
        Assert.assertEquals(joins2, mockRegistry.getJoinGraph(Group.class, "statuses"));
    }

    @Test
    public void testQueryWithSingleJoinGraph() {
        joinGraphRegistry.registerJoinGraph("fullUsers", Collections.singletonList(J.left(QUser.user1).nested(J.left(QStatus.status))), Group.class);

        List<Group> groups = joiner.find(Q.from(QGroup.group).joinGraphs("fullUsers"));

        assertUserAndStatusesFetched(groups, false);
    }

    @Test
    public void testQueryWithMultipleJoinGraph() {
        List<Group> groups = joiner.find(Q.from(QGroup.group).joinGraphs("statuses", "users", "userStatuses"));

        assertUserAndStatusesFetched(groups, true);
    }

    @Test
    public void testChildrenAggregated() {
        JoinerQueryBase<Group, Group> request = Q.from(QGroup.group).joinGraphs("users", "usersAddress", "userStatuses", "addressStatuses");
        joiner.find(request);
        Assert.assertEquals(1, request.getJoins().size());
        JoinDescription userJoin = request.getJoins().iterator().next();
        Assert.assertEquals(QUser.user1, userJoin.getAlias());
        Assert.assertEquals(2, userJoin.getChildren().size());

        Iterator<JoinDescription> children = userJoin.getChildren().iterator();
        JoinDescription first = children.next();

        // Address and its nested status
        Assert.assertEquals(J.path(QUser.user1, QAddress.address), first.getAlias());
        Assert.assertEquals(1, first.getChildren().size());

        Assert.assertEquals(J.path(QUser.user1, QStatus.status), children.next().getAlias());
    }

    @Test
    public void testChildrenNotOverrided() {
        JoinerQueryBase<Group, Group> request = Q.from(QGroup.group).joinGraphs("addressStatuses", "usersAddress", "userStatuses", "users");
        joiner.find(request);
        Assert.assertEquals(1, request.getJoins().size());
        JoinDescription userJoin = request.getJoins().iterator().next();
        Assert.assertEquals(QUser.user1, userJoin.getAlias());
        Assert.assertEquals(2, userJoin.getChildren().size());

        Iterator<JoinDescription> children = userJoin.getChildren().iterator();
        JoinDescription first = children.next();

        // Address and its nested status
        Assert.assertEquals(J.path(QUser.user1, QAddress.address), first.getAlias());
        Assert.assertEquals(1, first.getChildren().size());

        Assert.assertEquals(J.path(QUser.user1, QStatus.status), children.next().getAlias());
    }

    private void assertUserAndStatusesFetched(List<Group> groups, boolean isGroupStatusFetched) {
        Assert.assertFalse(groups.isEmpty());

        for (Group group : groups) {
            Assert.assertTrue(isLoaded(group, "users"));
            Assert.assertEquals(isGroupStatusFetched, isLoaded(group, "statuses"));
            for (User user : group.getUsers()) {
                Assert.assertTrue(isLoaded(user, "statuses"));
            }
        }
    }

}
