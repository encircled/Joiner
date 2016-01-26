package cz.encircled.joiner.test;

import java.util.List;

import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;

/**
 * @author Kisel on 26.01.2016.
 */
public class PredicateTest extends AbstractTest {

    @Test
    public void basicPredicateTest() {
        String name = "user1";
        List<User> result = userRepository.find(Q.from(QUser.user).where(QUser.user.name.eq(name)));
        assertHasName(result, name);
    }

    @Test
    public void predicateInCollectionTest() {
        String name = "user1street1";
        List<User> result = userRepository.find(Q.from(QUser.user)
                .addJoin(new JoinDescription(QUser.user.addresses))
                .where(QAddress.address.name.eq(name)));
        assertHasName(result, "user1");
        assertHasName(result.get(0).getAddresses(), name);
    }

}
