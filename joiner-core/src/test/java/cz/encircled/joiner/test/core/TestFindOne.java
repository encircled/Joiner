package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QUser;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Vlad on 11-Feb-17.
 */
public class TestFindOne extends AbstractTest {

    @Test(expected = JoinerException.class)
    public void testFindOneMultipleResults() {
        joiner.findOne(Q.from(QUser.user1));
    }

    @Test
    public void findOneReturnNull() {
        Assert.assertNull(joiner.findOne(Q.from(QUser.user1).where(QUser.user1.id.isNull())));
    }

}
