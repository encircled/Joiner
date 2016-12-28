package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 28-Dec-16.
 */
public class JoinRootTest {

    @Test
    public void testQueryGetJoin() {
        JoinerQueryBase<User, User> query = Q.from(QUser.user1).addHint("", null);

        Assert.assertNull(query.getJoin(QGroup.group));
        query.joins(QGroup.group);
        Assert.assertNotNull(query.getJoin(QGroup.group));
    }

    @Test
    public void testGetNestedJoin() {
        JoinerQueryBase<User, User> query = Q.from(QUser.user1)
                .joins(J.left(QGroup.group))
                .addHint("", null);

        Assert.assertNull(query.getJoin(QGroup.group).getJoin(QStatus.status));
        query.getJoin(QGroup.group).nested(QStatus.status);
        Assert.assertNotNull(query.getJoin(QGroup.group).getJoin(QStatus.status));
    }

}
