package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.JoinerException;
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
        joinGraphRegistry.registerJoinGraph("statuses", Collections.singletonList(J.left(QStatus.status)), Group.class);
        joinGraphRegistry.registerJoinGraph("users", Collections.singletonList(J.left(QUser.user1)), Group.class);
        joinGraphRegistry.registerJoinGraph("userStatuses", Collections.singletonList(J.left(QUser.user1).nested(J.left(QStatus.status))), Group.class);

        List<Group> groups = joiner.find(Q.from(QGroup.group).joinGraphs("statuses", "users", "userStatuses"));

        assertUserAndStatusesFetched(groups, true);
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
