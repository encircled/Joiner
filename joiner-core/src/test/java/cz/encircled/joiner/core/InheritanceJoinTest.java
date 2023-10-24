package cz.encircled.joiner.core;

import cz.encircled.joiner.model.*;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Kisel on 28.01.2016.
 */
public abstract class InheritanceJoinTest extends AbstractTest {

    @BeforeEach
    public void before(TestInfo testInfo) {
        super.beforeEach(testInfo);
        Assumptions.assumeFalse(isEclipse());

        entityManager.clear();
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    @Disabled
    public void testNestedOneToMany() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .joins(J.left(new QPhone("contacts")).nested(J.left(QStatus.status))));

        assertFalse(users.isEmpty());
        for (User user : users) {
            assertTrue(isLoaded(user, "contacts"));
            for (Contact contact : user.getContacts()) {
                assertTrue(isLoaded(contact, "statuses"));
            }
        }
    }

    @Test
    @Disabled
    public void testNestedManyToOne() {
        List<Contact> contacts = joiner.find(Q.from(QContact.contact)
                .joins(
                        J.left(new QNormalUser("employmentUser")).nested(J.left(QPassword.password))
                ));

        assertFalse(contacts.isEmpty());

        for (Contact contact : contacts) {
            assertTrue(isLoaded(contact, "employmentUser"));
            assertTrue(isLoaded(contact.getEmploymentUser(), "passwords"));
        }
    }

    @Test
    @Disabled
    public void testNestedManyToManyNotFetched() {
        List<Group> groups = joiner.find(Q.from(QGroup.group));

        assertFalse(groups.isEmpty());
        for (Group group : groups) {
            assertFalse(isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof NormalUser) {
                    assertFalse(isLoaded(user, "passwords"));
                }
            }
        }
    }

    @Test
    @Disabled
    public void testNestedManyToMany() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(
                        J.left(QNormalUser.normalUser).nested(J.left(QPassword.password))
                ));

        assertFalse(groups.isEmpty());
        for (Group group : groups) {
            assertTrue(isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                assertTrue(isLoaded(user, "passwords"));
            }
        }
    }

    @Test
    @Disabled
    public void testNestedDepth() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(
                        J.left(QUser.user1)
                                .nested(
                                        J.left(new QPhone("employmentContacts")).nested(J.left(QStatus.status)),
                                        J.left(QPassword.password).nested(J.left(QNormalUser.normalUser))
                                )
                ));

        assertFalse(groups.isEmpty());

        boolean atLeastOneUser = false;

        for (Group group : groups) {
            assertTrue(isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                assertTrue(isLoaded(user, "employmentContacts"));
                for (Contact userContact : user.getEmploymentContacts()) {
                    if (userContact instanceof Phone) {
                        assertTrue(isLoaded(userContact, "statuses"));
                    }
                }

                if (user instanceof NormalUser) {
                    assertTrue(isLoaded(user, "passwords"));
                    for (Password password : ((NormalUser) user).getPasswords()) {
                        assertTrue(isLoaded(password, "normalUser"));
                        atLeastOneUser = true;
                    }
                }
            }
        }

        assertTrue(atLeastOneUser);
    }

    @Test
    @Disabled
    public void joinSingleAndCollectionMultipleChildrenTest() {
        List<Group> groups = joiner.find(
                Q.from(QGroup.group)
                        .joins(J.left(QUser.user1)
                                .nested(
                                        J.left(QKey.key),
                                        J.left(QPassword.password)
                                ))
                        .where(QKey.key.name.ne("bad_key").or(QPassword.password.name.ne("bad_password")))
        );

        check(groups, true, true);
    }

    @Test
    @Disabled
    public void joinCollectionOnChildTest() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QNormalUser.normalUser).nested(J.left(QPassword.password)))
        );

        check(groups, false, true);
    }

    private void check(List<Group> groups, boolean key, boolean password) {
        boolean hasKey = false;
        boolean hasPassword = false;

        for (Group group : groups) {
            assertTrue(isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    if (key) {
                        assertTrue(isLoaded(user, "key"));
                    }
                    hasKey = true;
                }
                if (user instanceof NormalUser) {
                    if (password) {
                        assertTrue(isLoaded(user, "passwords"));
                    }
                    hasPassword = true;
                }
            }
        }

        if (key) {
            assertTrue(hasKey);
        }
        if (password) {
            assertTrue(hasPassword);
        }
    }

}
