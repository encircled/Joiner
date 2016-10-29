package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Objects;

/**
 * @author Kisel on 30.9.2016.
 */
public class PaginationAndOrderTest extends AbstractTest {

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

    @Test
    public void testOrderAsc() {
        List<User> users = joiner.find(Q.from(QUser.user1).asc(QUser.user1.name));

        Assert.assertFalse(users.isEmpty());
        Assert.assertTrue(users.size() > 1);

        Assert.assertTrue(isSorted(users, false));
    }

    @Test
    public void testOrderDesc() {
        List<User> users = joiner.find(Q.from(QUser.user1).desc(QUser.user1.name));

        Assert.assertFalse(users.isEmpty());
        Assert.assertTrue(users.size() > 1);

        Assert.assertTrue(isSorted(users, true));
    }

    @Test
    public void testOrderAscMultiple() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .desc(QUser.user1.name)
                .asc(QUser.user1.id)
                .where(QUser.user1.name.eq("user2")));

        Assert.assertFalse(users.isEmpty());
        Assert.assertTrue(users.size() > 1);

        Assert.assertTrue(isSorted(users, true));
        Assert.assertTrue(users.get(0).getId() < users.get(1).getId());
    }

    @Test
    public void testOrderDescMultiple() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .desc(QUser.user1.name)
                .desc(QUser.user1.id)
                .where(QUser.user1.name.eq("user2")));


        Assert.assertFalse(users.isEmpty());
        Assert.assertTrue(users.size() > 1);

        Assert.assertTrue(isSorted(users, true));
        Assert.assertTrue(users.get(0).getId() > users.get(1).getId());
    }

    @Test
    public void testNestedPropertyOrdering() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(QUser.user1)
                .asc(QGroup.group.users.any().name)
        );
    }

    @Test(expected = AliasMissingException.class)
    public void testOrderAliasMissing() {
        joiner.find(Q.from(QUser.user1).asc(QStatus.status.name));
    }

    public boolean isSorted(List<User> users, boolean isDesc) {
        for (int i = 1; i < users.size(); i++) {
            if (isDesc) {
                if (Objects.compare(users.get(i), users.get(i - 1), (o1, o2) -> o1.getName().compareTo(o2.getName())) > 0) {
                    return false;
                }
            } else {
                if (Objects.compare(users.get(i - 1), users.get(i), (o1, o2) -> o1.getName().compareTo(o2.getName())) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

}
