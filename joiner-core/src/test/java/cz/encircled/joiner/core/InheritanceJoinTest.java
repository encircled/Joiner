package cz.encircled.joiner.core;

import cz.encircled.joiner.model.*;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import javax.persistence.Persistence;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by Kisel on 28.01.2016.
 */
public class InheritanceJoinTest extends AbstractTest {

    @BeforeEach
    public void before(TestInfo testInfo) {
        super.beforeEach(testInfo);
        Assumptions.assumeFalse(isEclipse());

        entityManager.clear();
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    @Test
    public void testNestedOneToMany() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .joins(J.left(new QPhone("contacts")).nested(J.left(QStatus.status))));

        assertFalse(users.isEmpty());
        for (User user : users) {
            assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
            for (Contact contact : user.getContacts()) {
                assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "statuses"));
            }
        }
    }

    @Test
    public void testNestedManyToOne() {
        List<Contact> contacts = joiner.find(Q.from(QContact.contact)
                .joins(
                        J.left(new QNormalUser("employmentUser")).nested(J.left(QPassword.password))
                ));

        assertFalse(contacts.isEmpty());

        for (Contact contact : contacts) {
            assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "employmentUser"));
            assertTrue(Persistence.getPersistenceUtil().isLoaded(contact.getEmploymentUser(), "passwords"));
        }
    }

    @Test
    public void testNestedManyToManyNotFetched() {
        List<Group> groups = joiner.find(Q.from(QGroup.group));

        assertFalse(groups.isEmpty());
        for (Group group : groups) {
            assertFalse(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof NormalUser) {
                    assertFalse(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
                }
            }
        }
    }

    @Test
    public void testNestedManyToMany() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(
                        J.left(QNormalUser.normalUser).nested(J.left(QPassword.password))
                ));

        assertFalse(groups.isEmpty());
        for (Group group : groups) {
            assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
            }
        }
    }

    @Test
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
            assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
                for (Contact userContact : user.getEmploymentContacts()) {
                    if (userContact instanceof Phone) {
                        assertTrue(Persistence.getPersistenceUtil().isLoaded(userContact, "statuses"));
                    }
                }

                if (user instanceof NormalUser) {
                    assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
                    for (Password password : ((NormalUser) user).getPasswords()) {
                        assertTrue(Persistence.getPersistenceUtil().isLoaded(password, "normalUser"));
                        atLeastOneUser = true;
                    }
                }
            }
        }

        assertTrue(atLeastOneUser);
    }

    @Test
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
            assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    if (key) {
                        assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "key"));
                    }
                    hasKey = true;
                }
                if (user instanceof NormalUser) {
                    if (password) {
                        assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
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
