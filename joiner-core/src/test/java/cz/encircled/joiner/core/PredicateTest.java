package cz.encircled.joiner.core;

import cz.encircled.joiner.model.Address;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        List<Address> result = joiner.find(Q.from(QAddress.address)
                .joins(J.left(QUser.user1))
                .where(QAddress.address.user.name.eq(name)));

        for (Address address : result) {
            assertHasName(address.getUser(), name);
        }
    }

}
