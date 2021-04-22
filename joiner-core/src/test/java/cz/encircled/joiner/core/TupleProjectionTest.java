package cz.encircled.joiner.core;

import com.querydsl.core.Tuple;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Vlad on 11-Feb-17.
 */
public class TupleProjectionTest extends AbstractTest {

    @Test
    public void testSingleTuple() {
        List<Long> ids = joiner.find(Q.select(QUser.user1.id).from(QUser.user1));

        Assert.assertFalse(ids.isEmpty());
    }

    @Test
    public void testArrayTuple() {
        List<Tuple> tuple = joiner.find(Q.select(QUser.user1.id, QUser.user1.name).from(QUser.user1));

        Assert.assertFalse(tuple.isEmpty());
        Assert.assertEquals(2, tuple.get(0).size());
        Assert.assertNotNull(tuple.get(0).get(0, Long.class));
        Assert.assertNotNull(tuple.get(0).get(1, String.class));
    }

}
