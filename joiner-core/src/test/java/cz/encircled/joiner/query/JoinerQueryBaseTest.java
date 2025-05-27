package cz.encircled.joiner.query;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class JoinerQueryBaseTest {

    @Test
    public void testJoinerQueryBaseCopy() {
        JoinerQueryBase<User, Object> q = new JoinerQueryBase<>(QUser.user1);
        q.limit(1).asc(QUser.user1.id).addJoin(J.left(QGroup.group));

        JoinerQuery<User, Object> copy = q.copy();
        Assertions.assertEquals(q, copy);
        Assertions.assertEquals(1, copy.getLimit());
        Assertions.assertEquals(new QueryOrder(true, QUser.user1.id), copy.getOrder().get(0));
        Assertions.assertEquals(J.left(QGroup.group), copy.getJoins().iterator().next());
    }

}
