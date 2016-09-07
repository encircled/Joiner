package cz.encircled.joiner.test.core;

import cz.encircled.joiner.test.model.Contact;
import cz.encircled.joiner.test.model.User;
import org.eclipse.persistence.config.QueryHints;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.List;

/**
 * @author Vlad on 06-Sep-16.
 */
public class InheritanceJoiningCustomizerTest extends AbstractTest {

    @Test
    public void testNestedAssociationOnChildFetched() {
        Query query = entityManager.createQuery("select u from User u left join treat (u.contacts as Phone) p left join fetch p.statuses s");
        query.setHint(QueryHints.LEFT_FETCH, "u.contacts.statuses");
        List<User> users = query.getResultList();

        Assert.assertFalse(users.isEmpty());
        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
            for (Contact contact : user.getContacts()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "statuses"));
            }
        }
    }

}
