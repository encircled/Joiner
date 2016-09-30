package cz.encircled.joiner.test.core;

import java.util.Objects;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QAddress;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Kisel on 30.9.2016.
 */
public class PaginationTest extends AbstractTest {

    @Test
    public void testLimit() {
        Assert.assertEquals(1, joiner.find(Q.from(QAddress.address).limit(1L)).size());
        Assert.assertEquals(2, joiner.find(Q.from(QAddress.address).limit(2L)).size());
    }

    @Test
    public void testLimitWithOffset() {
        Long firstPage = joiner.find(Q.from(QAddress.address).offset(0L).limit(1L)).get(0).getId();
        Long secondPage = joiner.find(Q.from(QAddress.address).offset(1L).limit(1L)).get(0).getId();
        Assert.assertTrue(!Objects.equals(secondPage, firstPage));
    }

}
