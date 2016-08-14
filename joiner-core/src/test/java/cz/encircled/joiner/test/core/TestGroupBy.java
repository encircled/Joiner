package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QAddress;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * @author Kisel on 28.01.2016.
 */
public class TestGroupBy extends AbstractTest {

    @Test
    public void testGroupBy() {
        List<Double> avg = joiner.find(
                Q.from(QAddress.address).groupBy(QAddress.address.user),
                QAddress.address.id.avg()
        );
        Assert.assertTrue(avg.size() > 0);
        Assert.assertTrue(avg.size() < joiner.find(Q.from(QAddress.address)).size());
    }

    @Test
    public void testGroupByHaving() {
        List<Double> avg = joiner.find(
                Q.from(QAddress.address)
                        .groupBy(QAddress.address.user)
                        .having(QAddress.address.id.count().gt(2)),
                QAddress.address.id.avg()
        );
        Assert.assertTrue(avg.isEmpty());
    }

}
