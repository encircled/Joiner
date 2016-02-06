package cz.encircled.joiner.test.core.resolver;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QStatusType;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Test;

/**
 * @author Kisel on 27.01.2016.
 */
public class CollisionNoResolverAliasJoinTest extends AbstractTest {

    @Test
    public void collisionAliasCollectionJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .joins(J.joins(QGroup.group.statuses, QGroup.group.users))
                .join(J.join(QUser.user1.statuses).alias(new QStatus("userStatus"))));
    }

    @Test
    public void nestedCollisionAliasCollectionAndSingleNoCollisionJoinTest() {
        QStatus groupStatus = new QStatus("groupStatus");

        groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.statuses).alias(groupStatus))
                .joins(J.joins(groupStatus.statusType, QGroup.group.users, QUser.user1.statuses))
        );
    }

    @Test
    public void nestedCollisionAliasCollectionAndSingleJoinTest() {
        QStatus groupStatus = new QStatus("groupStatus");

        groupRepository.find(Q.from(QGroup.group)
                .join(J.join(QGroup.group.statuses).alias(groupStatus))
                .join(J.join(groupStatus.statusType).alias(new QStatusType("statusType2")))
                .joins(J.joins(QGroup.group.users, QUser.user1.statuses, QStatus.status.statusType))
        );
    }

}