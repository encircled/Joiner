package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QPhone;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 26.01.2016.
 */
@ContextConfiguration(classes = {TestConfig.class})
public class CollisionAliasJoinTest extends AbstractTest {

    @Test
    public void collisionAliasCollectionJoinTest() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QStatus.status), J.left(QUser.user1).nested(J.left(QStatus.status))));
    }


    @Test
    public void testTransientFieldIgnored() {
        joiner.find(Q.from(QUser.user1).joins(QPhone.phone));
    }

}
