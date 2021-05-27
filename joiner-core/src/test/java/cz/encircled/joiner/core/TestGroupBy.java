package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kisel on 28.01.2016.
 */
public class TestGroupBy extends AbstractTest {

    @Test
    public void testGroupBy() {
        List<Double> avg = joiner.find(
                Q.select(QAddress.address.id.avg())
                        .from(QAddress.address).groupBy(QAddress.address.user)
        );
        assertTrue(avg.size() > 0);
        assertTrue(avg.size() < joiner.find(Q.from(QAddress.address)).size());
    }

    @Test
    public void testGroupByHaving() {
        List<Double> avg = joiner.find(
                Q.select(QAddress.address.id.avg())
                        .from(QAddress.address)
                        .groupBy(QAddress.address.user)
                        .having(QAddress.address.id.count().gt(2))
        );
        assertTrue(avg.isEmpty());
    }

}
