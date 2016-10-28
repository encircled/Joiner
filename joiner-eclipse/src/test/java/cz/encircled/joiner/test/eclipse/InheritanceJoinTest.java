package cz.encircled.joiner.test.eclipse;

import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.test.model.Contact;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.NormalUser;
import cz.encircled.joiner.test.model.Password;
import cz.encircled.joiner.test.model.Phone;
import cz.encircled.joiner.test.model.QContact;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QKey;
import cz.encircled.joiner.test.model.QNormalUser;
import cz.encircled.joiner.test.model.QPassword;
import cz.encircled.joiner.test.model.QPhone;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QSuperUser;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.SuperUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Kisel on 28.01.2016.
 */
public class InheritanceJoinTest extends AbstractEclipseTest {

    @Test
    public void testNestedOneToMany() {
        List<User> users = joiner.find(Q.from(QUser.user1)
                .joins(J.left(new QPhone("contacts")).nested(J.left(QStatus.status))));

        Assert.assertFalse(users.isEmpty());
        for (User user : users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"));
            for (Contact contact : user.getContacts()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "statuses"));
            }
        }
    }

    @Test
    public void testNestedManyToOne() {
        List<Contact> contacts = joiner.find(Q.from(QContact.contact)
                .joins(
                        J.left(new QNormalUser("employmentUser")).nested(J.left(QPassword.password))
                ));

        Assert.assertFalse(contacts.isEmpty());

        for (Contact contact : contacts) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "employmentUser"));
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact.getEmploymentUser(), "passwords"));
        }
    }

    @Test
    public void testNestedManyToManyNotFetched() {
        List<Group> groups = joiner.find(Q.from(QGroup.group));

        Assert.assertFalse(groups.isEmpty());
        for (Group group : groups) {
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof NormalUser) {
                    Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
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

        Assert.assertFalse(groups.isEmpty());
        for (Group group : groups) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
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

        Assert.assertFalse(groups.isEmpty());

        boolean atLeastOneUser = false;

        for (Group group : groups) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"));
                for (Contact userContact : user.getEmploymentContacts()) {
                    if (userContact instanceof Phone) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(userContact, "statuses"));
                    }
                }

                if (user instanceof NormalUser) {
                    Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
                    for (Password password : ((NormalUser) user).getPasswords()) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(password, "normalUser"));
                        atLeastOneUser = true;
                    }
                }
            }
        }

        Assert.assertTrue(atLeastOneUser);
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
                        .where(J.path(QUser.user1, QKey.key).name.ne("bad_key"))
        );

        check(groups, true, true);
    }

    @Test
    public void joinCollectionOnChildTest() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(new JoinDescription(QNormalUser.normalUser._super).nested(J.left(QPassword.password)))
        );

        check(groups, false, true);
    }

    @Test
    public void testNotFoundSubclassPredicated() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                                .nested(J.left(QKey.key)),
                        J.left(QStatus.status))
                .where(J.path(QSuperUser.superUser, QKey.key).name.eq("not_exists"))
        );

        Assert.assertTrue(groups.isEmpty());
    }

    @Test
    public void testSubclassInCollectionJoined() {
        joiner.find(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(QKey.key)));
    }

    @Test
    public void testFoundSubclassPredicated() {
        List<Group> groups = joiner.find(Q.from(QGroup.group)
                .joins(J.left(QSuperUser.superUser)
                        .nested(J.left(QKey.key)))
                .joins(J.left(QStatus.status))
                .where(QSuperUser.superUser.key.name.eq("key1"))
        );

        Assert.assertFalse(groups.isEmpty());

        for (Group group : groups) {
            boolean hasKey = false;
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    SuperUser superUser = (SuperUser) user;
                    if (superUser.getKey() != null && superUser.getKey().getName().equals("key1")) {
                        hasKey = true;
                    }
                }
            }
            Assert.assertTrue(hasKey);
        }
    }

    private void check(List<Group> groups, boolean key, boolean password) {
        boolean hasKey = false;
        boolean hasPassword = false;

        for (Group group : groups) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(group, "users"));
            for (User user : group.getUsers()) {
                if (user instanceof SuperUser) {
                    if (key) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "key"));
                    }
                    hasKey = true;
                }
                if (user instanceof NormalUser) {
                    if (password) {
                        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "passwords"));
                    }
                    hasPassword = true;
                }
            }
        }

        if (key) {
            Assert.assertTrue(hasKey);
        }
        if (password) {
            Assert.assertTrue(hasPassword);
        }
    }


}
