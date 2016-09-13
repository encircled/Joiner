package cz.encircled.joiner.test.core;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Kisel on 26.01.2016.
 */
public class FailTest extends AbstractTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNullInput() {
        joiner.find(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullProjection() {
        joiner.find(Q.select((EntityPath<?>[]) null).from(QAddress.address));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullRequest() {
        joiner.find(null);
    }

    @Test(expected = AliasMissingException.class)
    public void predicateNoAliasTest() {
        joiner.find(Q.from(QUser.user1).where(QAddress.address.name.eq("user1street1")));
    }

    @Test(expected = JoinerException.class)
    public void testRightJoinFetch() {
        joiner.find(Q.from(QGroup.group).joins(J.left(QUser.user1).right()));
    }

    @Test(expected = AliasMissingException.class)
    public void testGroupByNoAlias() {
        List<Double> avg = joiner.find(
                Q.select(QAddress.address.id.avg())
                        .from(QAddress.address).groupBy(QGroup.group.name)
        );
        Assert.assertTrue(avg.size() > 0);
        Assert.assertTrue(avg.size() < joiner.find(Q.from(QAddress.address)).size());
    }

    @Test(expected = AliasMissingException.class)
    public void testGroupByHavingNoAlias() {
        List<Double> avg = joiner.find(
                Q.select(QAddress.address.id.avg()).from(QAddress.address)
                        .groupBy(QAddress.address.user)
                        .having(QGroup.group.id.count().gt(2))
        );
        Assert.assertTrue(avg.isEmpty());
    }

}
