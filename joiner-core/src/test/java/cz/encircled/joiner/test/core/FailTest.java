package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.AliasAlreadyUsedException;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.InsufficientSinglePathException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * @author Kisel on 26.01.2016.
 */
public class FailTest extends AbstractTest {

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testNullInput() {
        addressRepository.find(null);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testNullProjection() {
        addressRepository.find(Q.from(QAddress.address), null);
    }

    @Test(expected = InvalidDataAccessApiUsageException.class)
    public void testNullQ() {
        addressRepository.find(null, QUser.user1);
    }

    @Test(expected = InsufficientSinglePathException.class)
    public void testInsufficientSinglePath() {
        addressRepository.find(Q.from(QAddress.address)
                .addJoin(J.join(QUser.user1)));
    }

    @Test(expected = AliasMissingException.class)
    public void testCollectionAliasIsMissing() {
        groupRepository.find(Q.from(QGroup.group).addJoin(new JoinDescription(QUser.user1.addresses)));
    }

    @Test(expected = AliasMissingException.class)
    public void testSingleAliasIsMissing() {
        groupRepository.find(Q.from(QGroup.group).addJoin(new JoinDescription(QAddress.address.user)));
    }

    @Test(expected = AliasMissingException.class)
    public void predicateNoAliasTest() {
        userRepository.find(Q.from(QUser.user1).where(QAddress.address.name.eq("user1street1")));
    }

    @Test(expected = AliasAlreadyUsedException.class)
    public void nonCollisionAliasCollectionJoinTest() {
        groupRepository.find(Q.from(QGroup.group)
                .addJoins(J.joins(QGroup.group.statuses, QGroup.group.users, QUser.user1.statuses)));
    }

    @Test(expected = JoinerException.class)
    public void testRightJoinFetch() {
        groupRepository.find(Q.from(QGroup.group).addJoin(J.join(QGroup.group.users).right()));
    }

}
