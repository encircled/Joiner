package cz.encircled.joiner.core;

import cz.encircled.joiner.model.*;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cz.encircled.joiner.model.QUser.user1;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Kisel on 26.01.2016.
 */
public abstract class PredicateTest extends AbstractTest {

    @Test
    public void basicPredicateTest() {
        String name = "user1";
        List<User> result = joiner.find(Q.from(user1).where(user1.name.eq(name)));
        assertHasName(result, name);

        Address address = joiner.findOne(Q.from(QAddress.address).where(QAddress.address.city.eq(name + "city")).limit(1));
        assertNotNull(address);
        assertEquals(name + "city", address.getCity());
    }

    @Test
    public void whereAppendTest() {
        assertEquals(user1.name.eq("1"), Q.from(user1).orWhere(user1.name.eq("1")).getWhere());

        JoinerQuery<User, User> query = Q.from(user1);
        query.andWhere(user1.name.eq("1"));

        assertEquals(user1.name.eq("1"), query.getWhere());
        query.andWhere(user1.name.ne("2"));

        assertEquals(user1.name.ne("2").and(user1.name.eq("1")), query.getWhere());

        query.orWhere(user1.name.eq("3"));
        assertEquals(user1.name.eq("3").or(user1.name.ne("2").and(user1.name.eq("1"))), query.getWhere());

        assertEquals(user1.name.ne("3").and(user1.name.eq("1").or(user1.name.eq("2"))),
                Q.from(user1).andWhere(user1.name.eq("1").or(user1.name.eq("2"))).andWhere(user1.name.ne("3")).getWhere());

    }

    @Test
    public void predicateInCollectionTest() {
        String name = "user1street1";
        List<User> result = joiner.find(Q.from(user1)
                .joins(J.inner(QAddress.address))
                .where(QAddress.address.name.eq(name)));
        assertHasName(result, "user1");
        // Hibernate fetches all entities in a collection ()
        if (isEclipse()) {
            assertHasName(result.get(0).getAddresses(), name);
        }
    }

    @Test
    public void predicateInSinglePathTest() {
        String name = "user1";
        JoinerQueryBase<Address, Address> q = Q.from(QAddress.address)
                .joins(J.left(user1))
                .where(QAddress.address.user.name.eq(name));
        List<Address> result = joiner.find(q);

        assertQueryContains("select distinct address from Address address left join address.user user1 where address.user.name = ?1", q);

        for (Address address : result) {
            assertHasName(address.getUser(), name);
        }
    }

    @Test
    public void subQueryPredicate() {
        JoinerQuery<Address, Address> q = Q.from(QAddress.address)
                .where(QAddress.address.user.id.ne(Q.select(user1.id.max()).from(user1)));

        assertQueryContains("select distinct address from Address address where address.user.id <> (select max(user1.id) from User user1)", q);
        List<Address> addresses = joiner.find(q);
        assertFalse(addresses.isEmpty());
    }

    @Test
    public void anySubQueryPredicate() {
        JoinerQuery<User, User> q = Q.from(user1).where(user1.addresses.any().city.eq("user1city"));

        List<User> users = joiner.find(q);
        assertEquals(1, users.size());
        assertEquals("user1", users.get(0).getName());
    }

    @Test
    public void anyJoinSubQueryPredicate() {
        JoinerQuery<Group, Group> q = Q.from(QGroup.group).joins(J.inner(QGroup.group.users)).where(QGroup.group.users.any().addresses.any().city.eq("user1city"));

        List<Group> groups = joiner.find(q);
        assertEquals(1, groups.size());
        assertTrue(groups.get(0).getUsers().stream().anyMatch(u -> u.getName().equals("user1")));
    }

    @Test
    public void anyLeftJoinSubQueryPredicate() {
        JoinerQuery<Group, Group> q = Q.from(QGroup.group).joins(J.left(QGroup.group.users)).where(QGroup.group.users.any().addresses.any().city.eq("user1city"));

        List<Group> groups = joiner.find(q);
        assertEquals(1, groups.size());
        assertTrue(groups.get(0).getUsers().stream().anyMatch(u -> u.getName().equals("user1")));
    }

    @Test
    public void subQueryWithJoinPredicate() {
        JoinerQuery<Address, Address> q = Q.from(QAddress.address)
                .where(QAddress.address.user.id.ne(Q.select(user1.id.max()).from(QGroup.group).joins(J.inner(user1))));

        assertQueryContains("select distinct address from Address address where address.user.id <> (select max(user1.id) from Group group1 inner join group1.users user1)", q);
        List<Address> addresses = joiner.find(q);
        assertFalse(addresses.isEmpty());
    }

}
