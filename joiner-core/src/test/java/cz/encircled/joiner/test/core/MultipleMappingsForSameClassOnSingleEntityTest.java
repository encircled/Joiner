package cz.encircled.joiner.test.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.test.model.QContact;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Persistence;
import java.util.List;

/**
 * Tests for cases when an entity has multiple associations with the same class type (i.e. User and Contacts in test domain model)
 *
 * @author Vlad on 21-Aug-16.
 */
public class MultipleMappingsForSameClassOnSingleEntityTest extends AbstractTest {

    @Test(expected = JoinerException.class)
    public void testJoinAmbiguousAliasException() {
        joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)));
    }

    @Test
    public void testConflictResolvedAndFetched() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts"))));
        Assert.assertFalse(users.isEmpty());

        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
        }
    }

    @Test
    public void testAllConflictResolvedAndFetched() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts")), J.left(new QContact("contacts"))));
        Assert.assertFalse(users.isEmpty());

        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
        }
    }

    @Test
    public void testWrongValueNotCached() {
        joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts"))));
        try {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)));
            Assert.fail();
        } catch (JoinerException e) {
            // Expected
        }
    }

    @Test
    public void testWrongValueNotCached2() {
        try {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)));
        } catch (JoinerException e) {
            // Expected
        }
        joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts"))));
    }

}
