package cz.encircled.joiner.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.Contact;
import cz.encircled.joiner.model.Group;
import cz.encircled.joiner.model.QContact;
import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.Persistence;
import java.util.List;
import java.util.Objects;

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
    public void testAllConflictsResolvedAndFetched() {
        String s = joiner.toJPAQuery(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts")), J.left(new QContact("contacts")))).toString();
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts")), J.left(new QContact("contacts"))));
        Assert.assertFalse(users.isEmpty());

        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
        }
    }

    @Test(expected = JoinerException.class)
    public void testWrongAttributeNotMatchedByAlias() {
        joiner.find(Q.from(QContact.contact).joins(new QContact("user")));
    }

    @Test
    public void testManyToOneMapping() {
        List<Contact> result = joiner.find(Q.from(QContact.contact)
                .joins(J.left(new QUser("user")), J.left(new QUser("employmentUser"))));

        Assert.assertFalse(result.isEmpty());

        for (Contact contact : result) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "user"));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "employmentUser"));
            Assert.assertFalse(Objects.equals(contact.getUser().getId(), contact.getEmploymentUser().getId()));
        }
    }

    @Test
    public void testAllNestedConflictsResolvedAndFetched() {
        JoinerQuery<Group, Group> request = Q.from(QGroup.group).joins(
                J.left(QUser.user1)
                        .nested(J.left(new QContact("employmentContacts")).nested(J.left(new QUser("employmentUser"))), J.left(new QContact("contacts")))
        );
        List<Group> groups = joiner.find(request);
        Assert.assertFalse(groups.isEmpty());

        for (Group group : groups) {
            Assert.assertFalse(group.getUsers().isEmpty());
            for (User user : group.getUsers()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
            }
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
