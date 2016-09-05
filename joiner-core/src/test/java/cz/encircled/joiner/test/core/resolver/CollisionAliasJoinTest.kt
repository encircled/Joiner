package cz.encircled.joiner.test.core.resolver

import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.config.TestConfig
import cz.encircled.joiner.test.core.AbstractTest
import org.junit.Test
import org.springframework.test.context.ContextConfiguration

/**
 * @author Kisel on 26.01.2016.
 */
@ContextConfiguration(classes = arrayOf(TestConfig::class))
class CollisionAliasJoinTest : AbstractTest() {

    @Test
    fun collisionAliasCollectionJoinTest() {
        joiner.find(Q.from(QGroup.group).joins(J.left(QStatus.status), J.left(QUser.user1).nested(J.left(QStatus.status))))
    }

    @Test
    fun nestedCollisionAliasCollectionAndSingleJoinTest() {
        // TODO
        /*groupRepository.find(Q.from(QGroup.group)
                .joins(J.joins(QGroup.group.statuses,
                        TestAliasResolver.STATUS_ON_GROUP.statusType,
                        QGroup.group.users,
                        QUser.user1.statuses,
                        TestAliasResolver.STATUS_ON_USER.statusType))
        );*/
    }

}
