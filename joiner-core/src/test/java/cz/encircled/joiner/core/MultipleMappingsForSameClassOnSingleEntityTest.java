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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for cases when an entity has multiple associations with the same class type (i.e. User and Contacts in test domain model)
 *
 * @author Vlad on 21-Aug-16.
 */
public abstract class MultipleMappingsForSameClassOnSingleEntityTest extends AbstractTest {

    @Autowired
    public Joiner joiner2;

    @Test
    public void testJoinAmbiguousAliasException() {
        Assertions.assertThrows(JoinerException.class, () -> {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)));
        });
    }

    @Test
    public void testConflictResolvedAndFetched() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts"))));
        assertFalse(users.isEmpty());

        for (User user : users) {
            assertTrue(isLoaded(user, "employmentContacts"));
            assertFalse(isLoaded(user, "contacts"));
        }
    }

    @Test
    public void testAllConflictsResolvedAndFetched() {
        List<User> users = joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts")), J.left(new QContact("contacts"))));
        assertFalse(users.isEmpty());

        for (User user : users) {
            assertTrue(isLoaded(user, "employmentContacts"));
            assertTrue(isLoaded(user, "contacts"));
        }
    }

    @Test
    public void testWrongAttributeNotMatchedByAlias() {
        Assertions.assertThrows(JoinerException.class, () -> {
            joiner.find(Q.from(QContact.contact).joins(new QContact("user")));
        });
    }

    @Test
    public void testManyToOneMapping() {
        List<Contact> result = joiner.find(Q.from(QContact.contact)
                .joins(J.left(new QUser("user")), J.left(new QUser("employmentUser"))));

        assertFalse(result.isEmpty());

        for (Contact contact : result) {
            assertTrue(isLoaded(contact, "user"));
            assertTrue(isLoaded(contact, "employmentUser"));
            assertNotEquals(contact.getUser().getId(), contact.getEmploymentUser().getId());
        }
    }

    @Test
    public void testAllNestedConflictsResolvedAndFetched() {
        JoinerQuery<Group, Group> request = Q.from(QGroup.group).joins(
                J.left(QUser.user1)
                        .nested(J.left(new QContact("employmentContacts")).nested(J.left(new QUser("employmentUser"))), J.left(new QContact("contacts")))
        );
        List<Group> groups = joiner.find(request);
        assertFalse(groups.isEmpty());

        for (Group group : groups) {
            assertFalse(group.getUsers().isEmpty());
            for (User user : group.getUsers()) {
                assertTrue(isLoaded(user, "employmentContacts"));
                assertTrue(isLoaded(user, "contacts"));
            }
        }
    }

    @Test
    public void testWrongValueNotCached() {
        joiner.find(Q.from(QUser.user1).joins(J.left(new QContact("employmentContacts"))));
        try {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)));
            fail();
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
