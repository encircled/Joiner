package cz.encircled.joiner.core;

import cz.encircled.joiner.model.Address;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author Kisel on 26.01.2016.
 */
public abstract class PredicateTest extends AbstractTest {

    @Test
    public void basicPredicateTest() {
        String name = "user1";
        List<User> result = joiner.find(Q.from(QUser.user1).where(QUser.user1.name.eq(name)));
        assertHasName(result, name);
    }

    @Test
    public void predicateInCollectionTest() {
        String name = "user1street1";
        List<User> result = joiner.find(Q.from(QUser.user1)
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
                .joins(J.left(QUser.user1))
                .where(QAddress.address.user.name.eq(name));
        List<Address> result = joiner.find(q);

        assertQueryContains("select distinct address\n" +
                "from Address address\n" +
                "  left join address.user as user1\n" +
                "where address.user.name = ?1", q);

        for (Address address : result) {
            assertHasName(address.getUser(), name);
        }
    }

    @Test
    public void subQueryPredicate() {
        JoinerQuery<Address, Address> q = Q.from(QAddress.address)
                .where(QAddress.address.user.id.ne(Q.select(QUser.user1.id.max()).from(QUser.user1)));

        JoinerQuery<Address, Address> q2 = Q.from(QAddress.address)
                .where(QAddress.address.user.id.in(Q.select(QUser.user1.id.max()).from(QUser.user1)));

        assertQueryContains("select distinct address\n" +
                        "from Address address\n" +
                        "where address.user.id <> (select distinct max(user1.id)\n" +
                        "from User user1)", q);
        List<Address> addresses = joiner.find(q);
        assertFalse(addresses.isEmpty());
    }

    @Test
    public void subQueryWithJoinPredicate() {
        JoinerQuery<Address, Address> q = Q.from(QAddress.address)
                .where(QAddress.address.user.id.ne(Q.select(QUser.user1.id.max()).from(QGroup.group).joins(J.inner(QUser.user1))));

        assertQueryContains("select distinct address\n" +
                "from Address address\n" +
                "where address.user.id <> (select distinct max(user1.id)\n" +
                "from Group group1\n" +
                "  inner join group1.users as user1)", q);
        List<Address> addresses = joiner.find(q);
        assertFalse(addresses.isEmpty());
    }

}
