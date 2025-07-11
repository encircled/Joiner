package cz.encircled.joiner.core;

import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.model.AbstractEntity;
import cz.encircled.joiner.model.Address;
import cz.encircled.joiner.model.Group;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QStatus;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.Status;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Kisel on 30.9.2016.
 */
public abstract class PaginationAndOrderTest extends AbstractTest {

    @Test
    public void testLimit() {
        assertEquals(1, joiner.find(Q.from(QAddress.address).limit(1)).size());
        assertEquals(2, joiner.find(Q.from(QAddress.address).limit(2)).size());
    }

    @Test
    public void testLimitWithJoin() {
        List<Address> res = joiner.find(Q.from(QAddress.address).limit(5).joins(J.left(QStatus.status).fetch(false)));
        assertEquals(5, res.size());
    }

    @Test
    public void testLimitWithOffset() {
        Long firstPage = joiner.find(Q.from(QAddress.address).offset(0).limit(1)).get(0).getId();
        Long secondPage = joiner.find(Q.from(QAddress.address).offset(1).limit(1)).get(0).getId();
        assertNotEquals(secondPage, firstPage);
    }

    @Test
    public void testOrderAsc() {
        List<User> users = joiner.find(Q.from(QUser.user1).asc(QUser.user1.name));

        assertFalse(users.isEmpty());
        assertTrue(users.size() > 1);

        assertTrue(isSorted(users, false));
    }

    @Test
    public void testOrderAscFromJoin() {
        JoinerQueryBase<Status, Status> query = Q.select(QStatus.status).from(QStatus.status)
                .joins(J.inner(QGroup.group).nested(J.inner(new QUser("test"))))
                .asc(new QUser("test").name);

        assertQueryContains("select distinct status from Status status inner join status.group group1 inner join group1.users test_on_group1 order by test_on_group1.name asc", query);
    }

    @Test
    public void testOrderDesc() {
        List<User> users = joiner.find(Q.from(QUser.user1).desc(QUser.user1.name));

        assertFalse(users.isEmpty());
        assertTrue(users.size() > 1);

        assertTrue(isSorted(users, true));
    }

    @Test
    public void testOrderAscMultiple() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .desc(QUser.user1.name)
                .asc(QUser.user1.id)
                .where(QUser.user1.name.eq("user2")));

        assertFalse(users.isEmpty());
        assertTrue(users.size() > 1);

        assertTrue(isSorted(users, true));
        assertTrue(users.get(0).getId() < users.get(1).getId());
    }

    @Test
    public void testOrderDescMultiple() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .desc(QUser.user1.name)
                .desc(QUser.user1.id)
                .where(QUser.user1.name.eq("user2")));


        assertFalse(users.isEmpty());
        assertTrue(users.size() > 1);

        assertTrue(isSorted(users, true));
        assertTrue(users.get(0).getId() > users.get(1).getId());
    }

    @Test
    public void testNestedPropertyOrdering() {
        List<Address> addresses = joiner.find(Q.from(QAddress.address)
                .joins(QUser.user1)
                .asc(QUser.user1.name)
        );
        assertTrue(isSorted(addresses.stream().map(Address::getUser).toList(), false));
    }

    @Test
    public void testOrderAliasMissing() {
        assertThrows(AliasMissingException.class, () -> {
            joiner.find(Q.from(QUser.user1).asc(QStatus.status.name));
        });
    }

    public boolean isSorted(List<User> users, boolean isDesc) {
        for (int i = 1; i < users.size(); i++) {
            if (isDesc) {
                if (Objects.compare(users.get(i), users.get(i - 1), Comparator.comparing(AbstractEntity::getName)) > 0) {
                    return false;
                }
            } else {
                if (Objects.compare(users.get(i - 1), users.get(i), Comparator.comparing(AbstractEntity::getName)) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

}
