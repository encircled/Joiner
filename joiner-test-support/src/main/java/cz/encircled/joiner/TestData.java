package cz.encircled.joiner;

import cz.encircled.joiner.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * @author Kisel on 26.01.2016.
 */
@Component
public class TestData {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @Commit
    public void prepareData() {
        if (!entityManager.createQuery("select u from User u").setMaxResults(1).getResultList().isEmpty()) {
            return;
        }

        Group group = new Group();
        group.setName("group1");
        entityManager.persist(group);

        Status groupStatus = new Status();
        groupStatus.setName("status1group1");
        groupStatus.setGroup(group);
        group.setStatuses(Collections.singleton(groupStatus));
        entityManager.persist(groupStatus);

        baseUserCreate(group, 1, true);
        baseUserCreate(group, 2, true);
        baseUserCreate(group, 2, false);
        baseUserCreate(group, 3, false);
        normalUser(group, superUser(group));

        WithSelfReference parentRef = new WithSelfReference();
        entityManager.persist(parentRef);
        WithSelfReference ref = new WithSelfReference();
        ref.setName("refWithParent");
        ref.selfReference = parentRef;
        entityManager.persist(ref);

        entityManager.flush();
        entityManager.clear();
    }

    private void normalUser(Group group, User parent) {
        NormalUser user = new NormalUser();
        user.setUser(parent);
        user.setGroups(Collections.singletonList(group));
        user.setName("normalUser1");
        entityManager.persist(user);

        Password password = new Password();
        password.setName("normalUser1password1");
        password.setNormalUser(user);
        entityManager.persist(password);

        Address address = new Address();
        address.setName("normalUser1street1");
        address.setUser(user);
        entityManager.persist(address);

        Status addressStatus = new Status();
        addressStatus.setAddress(address);
        addressStatus.setName("Address Status");
        entityManager.persist(addressStatus);
        Status addressStatus2 = new Status();
        addressStatus2.setAddress(address);
        addressStatus2.setName("Address Status2");
        entityManager.persist(addressStatus2);

        SuperUser superUser = new SuperUser();
        superUser.setName("superUser2");
        superUser.setGroups(Collections.singletonList(group));
        entityManager.persist(superUser);

        Phone contact = new Phone();
        contact.setName("PhoneNumber");
        contact.setEmploymentUser(user);
        contact.setUser(superUser);
        entityManager.persist(contact);

        Status phoneStatus = new Status();
        phoneStatus.setPhone(contact);
        phoneStatus.setName("TestStatus");
        entityManager.persist(phoneStatus);
    }

    private User superUser(Group group) {
        Key key = new Key();
        key.setName("key1");
        entityManager.persist(key);

        SuperUser superUser = new SuperUser();
        superUser.setName("superUser1");
        superUser.setGroups(Collections.singletonList(group));
        superUser.setKey(key);
        entityManager.persist(superUser);

        Status superUserStatus = new Status();
        superUserStatus.setUser(superUser);
        superUserStatus.setName("SuperUserTestStatus");
        entityManager.persist(superUserStatus);

        return superUser;
    }

    private void baseUserCreate(Group group, int index, boolean withAddresses) {
        User user = new User();
        user.setName("user" + index);
        user.setGroups(Collections.singletonList(group));
        entityManager.persist(user);

        if (withAddresses) {
            Address address = new Address();
            address.setName("user" + index + "street1");
            address.setCity("user" + index + "city");
            address.setUser(user);

            Address address2 = new Address();
            address2.setName("user" + index + "street2");
            address2.setCity("user" + index + "city");
            address2.setUser(user);
            entityManager.persist(address);
            entityManager.persist(address2);
        }
    }


}
