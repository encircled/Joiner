package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.util.List;

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

    @Test(expected = AliasMissingException.class)
    public void predicateNoAliasTest() {
        userRepository.find(Q.from(QUser.user1).where(QAddress.address.name.eq("user1street1")));
    }

    @Test(expected = JoinerException.class)
    public void testRightJoinFetch() {
        groupRepository.find(Q.from(QGroup.group).joins(J.left(QUser.user1).right()));
    }

    @Test(expected = AliasMissingException.class)
    public void testGroupByNoAlias() {
        List<Double> avg = addressRepository.find(
                Q.from(QAddress.address).groupBy(QGroup.group.name),
                QAddress.address.id.avg()
        );
        Assert.assertTrue(avg.size() > 0);
        Assert.assertTrue(avg.size() < addressRepository.find(Q.from(QAddress.address)).size());
    }

    @Test(expected = AliasMissingException.class)
    public void testGroupByHavingNoAlias() {
        List<Double> avg = addressRepository.find(
                Q.from(QAddress.address)
                        .groupBy(QAddress.address.user)
                        .having(QGroup.group.id.count().gt(2)),
                QAddress.address.id.avg()
        );
        Assert.assertTrue(avg.isEmpty());
    }

}
