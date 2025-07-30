package cz.encircled.joiner.core;

import cz.encircled.joiner.model.Address;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cz.encircled.joiner.model.QAddress.address;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kisel on 28.01.2016.
 */
public abstract class GroupByTest extends AbstractTest {

    @Test
    public void testGroupBy() {
        List<Double> avg = joiner.find(
                Q.select(address.id.avg())
                        .from(address).groupBy(address.user)
        );
        assertFalse(avg.isEmpty());
        assertTrue(avg.size() < joiner.find(Q.from(address)).size());
    }

    @Test
    public void testGroupByMultiple() {
        JoinerQuery<Address, Double> query = Q.select(address.id.avg())
                .from(address).groupBy(address.user.id, address.city);
        List<Double> avg = joiner.find(query);
        assertTrue(query.toString().contains("group by address.user.id, address.city"));
        assertFalse(avg.isEmpty());
        assertTrue(avg.size() < joiner.find(Q.from(address)).size());
    }

    @Test
    public void testGroupByHaving() {
        List<Double> avg = joiner.find(
                Q.select(address.id.avg())
                        .from(address)
                        .groupBy(address.user)
                        .having(address.id.count().gt(2))
        );
        assertTrue(avg.isEmpty());
    }

}
