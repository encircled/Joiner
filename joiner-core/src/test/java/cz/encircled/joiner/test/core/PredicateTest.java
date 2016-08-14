package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.J;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;

import java.util.List;

/**
 * @author Kisel on 26.01.2016.
 */
public class PredicateTest extends AbstractTest {

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
                .joins(J.left(QAddress.address))
                .where(QAddress.address.name.eq(name)));
        assertHasName(result, "user1");
        assertHasName(result.get(0).getAddresses(), name);
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
