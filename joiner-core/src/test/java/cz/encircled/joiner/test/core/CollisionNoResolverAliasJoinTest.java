package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
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
                .addJoins(J.joins(QGroup.group.statuses, QGroup.group.users))
                .addJoin(J.join(QUser.user1.statuses).alias(new QStatus("userStatus"))));
    }

    @Test
    public void nestedCollisionAliasCollectionAndSingleNoCollisionJoinTest() {
        QStatus groupStatus = new QStatus("groupStatus");

        groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.statuses).alias(groupStatus))
                .addJoins(J.joins(groupStatus.statusType, QGroup.group.users, QUser.user1.statuses))
        );
    }

    @Test
    public void nestedCollisionAliasCollectionAndSingleJoinTest() {
        QStatus groupStatus = new QStatus("groupStatus");

        groupRepository.find(Q.from(QGroup.group)
                .addJoin(J.join(QGroup.group.statuses).alias(groupStatus))
                .addJoin(J.join(groupStatus.statusType).alias(new QStatusType("statusType2")))
                .addJoins(J.joins(QGroup.group.users, QUser.user1.statuses, QStatus.status.statusType))
        );
    }

}
