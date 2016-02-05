package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestAliasResolver;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.TestConfigWithResolver;
import cz.encircled.joiner.test.core.AbstractTest;
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
    public void collisionAliasCollectionJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .joins(J.joins(QGroup.group.statuses, QGroup.group.users, QUser.user1.statuses)));
    }

    @Test
    public void nestedCollisionAliasCollectionAndSingleJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .joins(J.joins(QGroup.group.statuses,
                        TestAliasResolver.STATUS_ON_GROUP.statusType,
                        QGroup.group.users,
                        QUser.user1.statuses,
                        TestAliasResolver.STATUS_ON_USER.statusType))
        );
    }

}
