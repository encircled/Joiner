package cz.encircled.joiner.test.core

import cz.encircled.joiner.exception.JoinerException
import cz.encircled.joiner.query.Q
import cz.encircled.joiner.query.join.J
import cz.encircled.joiner.test.model.Group
import org.junit.Assert
import org.junit.Test
import javax.persistence.Persistence

/**
 * Tests for cases when an entity has multiple associations with the same class type (i.e. User and Contacts in test domain model)

 * @author Vlad on 21-Aug-16.
 */
class MultipleMappingsForSameClassOnSingleEntityTest : AbstractTest() {

    @Test(expected = JoinerException::class)
    fun testJoinAmbiguousAliasException() {
        joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)))
    }

    @Test
    fun testConflictResolvedAndFetched() {
        val users = joiner.find(Q.from(QUser.user1).joins(J.left(QContact("employmentContacts"))))
        Assert.assertFalse(users.isEmpty())

        for (user in users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"))
            Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(user, "contacts"))
        }
    }

    @Test
    fun testAllConflictsResolvedAndFetched() {
        val users = joiner.find(Q.from(QUser.user1).joins(J.left(QContact("employmentContacts")), J.left(QContact("contacts"))))
        Assert.assertFalse(users.isEmpty())

        for (user in users) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"))
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"))
        }
    }

    /* TODO should throw an exception
                    List<Contact> result = joiner.find(Q.from(QContact.contact)
                            .joins(J.left(new QContact("user")), J.left(new QContact("employmentUser"))));
     */

    @Test
    fun testManyToOneMapping() {
        val result = joiner.find(Q.from(QContact.contact).joins(J.left(QUser("user")), J.left(QUser("employmentUser"))))

        Assert.assertFalse(result.isEmpty())

        for (contact in result) {
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "user"))
            Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(contact, "employmentUser"))
            Assert.assertFalse(contact.user.id == contact.employmentUser.id)
        }
    }

    @Test
    fun testAllNestedConflictsResolvedAndFetched() {
        val request = Q.from(QGroup.group).joins(
                J.left(QUser.user1).nested(J.left(QContact("employmentContacts")).nested(J.left(QUser("employmentUser"))), J.left(QContact("contacts"))))
        var groups = joiner.find<Group>(request)
        Assert.assertFalse(groups.isEmpty())

        for (group in groups) {
            Assert.assertFalse(group.users.isEmpty())
            for (user in group.users) {
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "employmentContacts"))
                Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(user, "contacts"))
            }
        }

        groups = joiner.find<Group>(request)
    }

    @Test
    fun testWrongValueNotCached() {
        joiner.find(Q.from(QUser.user1).joins(J.left(QContact("employmentContacts"))))
        try {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)))
            Assert.fail()
        } catch (e: JoinerException) {
            // Expected
        }

    }

    @Test
    fun testWrongValueNotCached2() {
        try {
            joiner.find(Q.from(QUser.user1).joins(J.left(QContact.contact)))
        } catch (e: JoinerException) {
            // Expected
        }

        joiner.find(Q.from(QUser.user1).joins(J.left(QContact("employmentContacts"))))
    }

}
