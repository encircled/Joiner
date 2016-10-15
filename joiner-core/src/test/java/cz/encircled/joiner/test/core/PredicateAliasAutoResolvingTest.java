package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

/**
 * @author Vlad on 15-Oct-16.
 */
public class PredicateAliasAutoResolvingTest extends AbstractTest {

    @Test
    @Ignore // TODO not supported?
    public void testNestedAliasResolved() {
        List<User> users = joiner.find(
                Q.from(QUser.user1)
                        .joins(J.inner(QGroup.group).nested(J.left(QStatus.status)))
                        .where(QStatus.status.isNotNull())
        );

        Assert.assertFalse(users.isEmpty());
        for (User user : users) {
            Assert.assertTrue(isLoaded(user, "groups"));

            Assert.assertFalse(user.getGroups().isEmpty());

            for (Group group : user.getGroups()) {
                Assert.assertTrue(isLoaded(group, "statuses"));

                Assert.assertFalse(user.getStatuses().isEmpty());
            }
        }
    }

}
