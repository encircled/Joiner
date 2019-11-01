package cz.encircled.joiner.test;

import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.spring.PageableFeature;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Kisel on 29.10.2016.
 */
public class PageableFeatureTest {

    @Test
    public void testLimitAndOffset() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);

        new PageableFeature(PageRequest.of(2, 10)).before(request);

        Assert.assertEquals(Long.valueOf(10L), request.getLimit());
        Assert.assertEquals(Long.valueOf(20L), request.getOffset());
    }

    @Test
    public void testAscSort() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        new PageableFeature(PageRequest.of(2, 10, asc("id"))).before(request);

        List<QueryOrder> orders = request.getOrder();
        Assert.assertEquals(1, orders.size());
        Assert.assertTrue(orders.get(0).isAsc());
        Assert.assertEquals(QUser.user1.id, orders.get(0).getTarget());
    }

    @Test
    public void testDescSort() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        new PageableFeature(PageRequest.of(2, 10, asc("id"))).before(request);

        List<QueryOrder> orders = request.getOrder();
        Assert.assertEquals(1, orders.size());
        Assert.assertFalse(orders.get(0).isAsc());
        Assert.assertEquals(QUser.user1.id, orders.get(0).getTarget());
    }

    @Test
    public void testMultipleSorts() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        new PageableFeature(PageRequest.of(2, 10, asc("id", "name"))).before(request);

        List<QueryOrder> orders = request.getOrder();
        Assert.assertEquals(2, orders.size());
        Assert.assertTrue(orders.get(0).isAsc());
        Assert.assertEquals(QUser.user1.id, orders.get(0).getTarget());
        Assert.assertTrue(orders.get(1).isAsc());
        Assert.assertEquals(QUser.user1.name, orders.get(1).getTarget());
    }

    @Test
    public void testNestedPropertySort() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);

        new PageableFeature(PageRequest.of(2, 10, Sort.by(Sort.Order.asc("groups.name")))).before(request);

        List<QueryOrder> orders = request.getOrder();
        Assert.assertEquals(1, orders.size());
        Assert.assertTrue(orders.get(0).isAsc());
        Assert.assertEquals("user1.groups.name", orders.get(0).getTarget().toString());
    }

    private Sort asc(String... props) {
        return Sort.by(Arrays.stream(props).map(Sort.Order::asc).collect(Collectors.toList()));
    }

    private Sort desc(String... props) {
        return Sort.by(Arrays.stream(props).map(Sort.Order::asc).collect(Collectors.toList()));
    }

}
