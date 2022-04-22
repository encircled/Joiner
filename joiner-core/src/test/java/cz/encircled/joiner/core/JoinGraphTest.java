package cz.encircled.joiner.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.Group;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.DefaultJoinGraphRegistry;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static cz.encircled.joiner.model.QGroup.group;
import static cz.encircled.joiner.model.QStatus.status;
import static cz.encircled.joiner.model.QUser.user1;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Vlad on 15-Aug-16.
 */
@Transactional
public class JoinGraphTest extends AbstractTest {

    private DefaultJoinGraphRegistry mockRegistry;

    @BeforeEach
    public void before(TestInfo testInfo) {
        super.beforeEach(testInfo);
        mockRegistry = new DefaultJoinGraphRegistry();

        try {
            joinGraphRegistry.getJoinGraph(Group.class, "users");
        } catch (Exception e) {
            joinGraphRegistry.registerJoinGraph("statuses", Collections.singletonList(J.left(status)), Group.class);
            joinGraphRegistry.registerJoinGraph("users", Collections.singletonList(J.left(user1)), Group.class);
            joinGraphRegistry.registerJoinGraph("userStatuses", Collections.singletonList(J.left(user1).nested(status)), Group.class);
            joinGraphRegistry.registerJoinGraph("usersAddress", Collections.singletonList(J.left(user1).nested(QAddress.address)), Group.class);
            joinGraphRegistry.registerJoinGraph("addressStatuses", Collections.singletonList(J.left(user1)
                    .nested(J.left(QAddress.address).nested(status))), Group.class);
        }
    }

    @Test
    public void testNullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            mockRegistry.registerJoinGraph(null, Collections.singletonList(J.left(user1)), Group.class);
        });
    }

    @Test
    public void testNullJoins() {
        assertThrows(IllegalArgumentException.class, () -> {
            mockRegistry.registerJoinGraph("test", null, Group.class);
        });
    }

    @Test
    public void testNullClass() {
        assertThrows(IllegalArgumentException.class, () -> {
            mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), null);
        });
    }

    @Test
    public void testEmptyClasses() {
        assertThrows(IllegalArgumentException.class, () -> {
            mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)));
        });
    }

    @Test
    public void testDuplicatedName() {
        assertThrows(JoinerException.class, () -> {
            mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), Group.class);
            mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), Group.class);
        });
    }

    @Test
    public void testGraphMissing() {
        assertThrows(JoinerException.class, () -> {
            mockRegistry.getJoinGraph(Group.class, "NotExists");
        });
    }

    @Test
    public void testReplaceGraph() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), Group.class);
        assertEquals(Collections.singletonList(J.left(user1)), mockRegistry.getJoinGraph(Group.class, "test"));

        mockRegistry.replaceJoinGraph("test", Collections.singletonList(J.left(status)), Group.class);
        assertEquals(Collections.singletonList(J.left(status)), mockRegistry.getJoinGraph(Group.class, "test"));
    }

    @Test
    public void testReplaceNonExistingGraph() {
        assertThrows(JoinerException.class, () -> {
            mockRegistry.replaceJoinGraph("test", Collections.singletonList(J.left(status)), Group.class);
        });
    }

    @Test
    public void testRegisterOrReplaceGraph() {
        mockRegistry.registerOrReplaceJoinGraph("test", Collections.singletonList(J.left(status)), Group.class);

        assertEquals(Collections.singletonList(J.left(status)), mockRegistry.getJoinGraph(Group.class, "test"));

        mockRegistry.registerOrReplaceJoinGraph("test", Collections.singletonList(J.left(user1)), Group.class);

        assertEquals(Collections.singletonList(J.left(user1)), mockRegistry.getJoinGraph(Group.class, "test"));
    }

    @Test
    public void testDuplicatedNameForDifferentClasses() {
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), Group.class);
        mockRegistry.registerJoinGraph("test", Collections.singletonList(J.left(user1)), User.class);
    }

    @Test
    public void testAddToRegistry() {
        List<JoinDescription> joins = Collections.singletonList(J.left(user1));
        List<JoinDescription> joins2 = Collections.singletonList(J.left(status));

        mockRegistry.registerJoinGraph("users", joins, Group.class);
        mockRegistry.registerJoinGraph("statuses", joins2, Group.class);

        assertEquals(joins, mockRegistry.getJoinGraph(Group.class, "users"));
        assertEquals(joins2, mockRegistry.getJoinGraph(Group.class, "statuses"));
    }

    @Test
    public void testQueryWithSingleJoinGraph() {
        joinGraphRegistry.registerJoinGraph("fullUsers", Collections.singletonList(J.left(user1).nested(J.left(status))), Group.class);

        List<Group> groups = joiner.find(Q.from(group).joinGraphs("fullUsers"));

        assertUserAndStatusesFetched(groups, false);
    }

    @Test
    public void testQueryWithMultipleJoinGraph() {
        List<Group> groups = joiner.find(Q.from(group).joinGraphs("statuses", "users", "userStatuses"));

        assertUserAndStatusesFetched(groups, true);
    }

    @Test
    public void testQueryWithCollectionJoinGraph() {
        List<Group> groups = joiner.find(Q.from(group).joinGraphs(Arrays.asList("statuses", "users", "userStatuses")));

        assertUserAndStatusesFetched(groups, true);
    }

    @Test
    public void testChildrenAggregated() {
        JoinerQuery<Group, Group> request = Q.from(group).joinGraphs("users", "usersAddress", "userStatuses", "addressStatuses");
        joiner.find(request);
        assertEquals(1, request.getJoins().size());
        JoinDescription userJoin = request.getJoins().iterator().next();
        assertEquals(user1, userJoin.getAlias());
        assertEquals(2, userJoin.getChildren().size());

        Iterator<JoinDescription> children = userJoin.getChildren().iterator();
        JoinDescription first = children.next();

        // Address and its nested status
        assertEquals(J.path(user1, QAddress.address), first.getAlias());
        assertEquals(1, first.getChildren().size());

        assertEquals(J.path(user1, status), children.next().getAlias());
    }

    @Test
    public void testChildrenNotOverrided() {
        JoinerQuery<Group, Group> request = Q.from(group).joinGraphs("addressStatuses", "usersAddress", "userStatuses", "users");
        joiner.find(request);
        assertEquals(1, request.getJoins().size());
        JoinDescription userJoin = request.getJoins().iterator().next();
        assertEquals(user1, userJoin.getAlias());
        assertEquals(2, userJoin.getChildren().size());

        Iterator<JoinDescription> children = userJoin.getChildren().iterator();
        JoinDescription first = children.next();

        // Address and its nested status
        assertEquals(J.path(user1, QAddress.address), first.getAlias());
        assertEquals(1, first.getChildren().size());

        assertEquals(J.path(user1, status), children.next().getAlias());
    }

    private void assertUserAndStatusesFetched(List<Group> groups, boolean isGroupStatusFetched) {
        assertFalse(groups.isEmpty());

        for (Group group : groups) {
            assertTrue(isLoaded(group, "users"));
            assertEquals(isGroupStatusFetched, isLoaded(group, "statuses"));
            for (User user : group.getUsers()) {
                assertTrue(isLoaded(user, "statuses"));
            }
        }
    }

}
