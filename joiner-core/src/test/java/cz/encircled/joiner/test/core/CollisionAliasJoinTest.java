package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.TestConfigWithResolver;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 26.01.2016.
 */
@ContextConfiguration(classes = { TestConfig.class, TestConfigWithResolver.class })
public class CollisionAliasJoinTest extends AbstractTest {

    @Test
    public void nonCollisionAliasCollectionJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.statuses))
                .addJoin(J.join(QGroup.group.users))
                .addJoin(J.join(QUser.user.statuses)));
    }

}
