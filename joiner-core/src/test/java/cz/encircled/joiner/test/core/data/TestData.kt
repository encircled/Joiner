package cz.encircled.joiner.test.core.data

import cz.encircled.joiner.test.model.*
import org.springframework.stereotype.Component
import org.springframework.test.annotation.Commit
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

/**
 * @author Kisel on 26.01.2016.
 */
@Component
class TestData {

    @PersistenceContext
    private val entityManager: EntityManager? = null

    @Transactional
    @Commit
    fun prepareData() {
        if (entityManager!!.createQuery("select u from User u").setMaxResults(1).resultList.size > 0) {
            return
        }

        val group = Group()
        group.name = "group1"
        entityManager.persist(group)

        baseUserCreate(group, 1, true)
        baseUserCreate(group, 2, true)
        baseUserCreate(group, 2, false)
        superUser(group)
        normalUser(group)

        entityManager.flush()
        entityManager.clear()
    }

    private fun normalUser(group: Group) {
        val user = NormalUser()
        user.groups = listOf(group)
        user.name = "normalUser1"
        entityManager!!.persist(user)

        val password = Password()
        password.name = "normalUser1password1"
        password.normalUser = user
        entityManager.persist(password)

        val address = Address()
        address.name = "normalUser1street1"
        address.user = user
        entityManager.persist(address)

        val superUser = SuperUser()
        superUser.name = "superUser2"
        superUser.groups = listOf(group)
        entityManager.persist(superUser)

        val contact = Contact()
        contact.name = "PhoneNumber"
        contact.employmentUser = user
        contact.user = superUser
        entityManager.persist(contact)
    }

    private fun superUser(group: Group) {
        val key = Key()
        key.name = "key1"
        entityManager!!.persist(key)

        val superUser = SuperUser()
        superUser.name = "superUser1"
        superUser.groups = listOf(group)
        superUser.key = key
        entityManager.persist(superUser)
    }

    private fun baseUserCreate(group: Group, index: Int, withAddresses: Boolean) {
        val user = User()
        user.name = "user" + index
        user.groups = listOf(group)
        entityManager!!.persist(user)

        if (withAddresses) {
            val address = Address()
            address.name = "user" + index + "street1"
            address.user = user

            val address2 = Address()
            address2.name = "user" + index + "street2"
            address2.user = user
            entityManager.persist(address)
            entityManager.persist(address2)
        }
    }


}
