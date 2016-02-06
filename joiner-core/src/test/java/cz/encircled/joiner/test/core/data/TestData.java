package cz.encircled.joiner.test.core.data;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.Key;
import cz.encircled.joiner.test.model.NormalUser;
import cz.encircled.joiner.test.model.Password;
import cz.encircled.joiner.test.model.SuperUser;
import cz.encircled.joiner.test.model.User;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

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
        if (entityManager.createQuery("select u from User u").setMaxResults(1).getResultList().size() > 0) {
            return;
        }

        Group group = new Group();
        group.setName("group1");
        entityManager.persist(group);

        baseUserCreate(group, 1, true);
        baseUserCreate(group, 2, true);
        baseUserCreate(group, 2, false);
        superUser(group);
        normalUser(group);

        entityManager.flush();
        entityManager.clear();
    }

    private void normalUser(Group group) {
        NormalUser user = new NormalUser();
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
    }

    private void superUser(Group group) {
        Key key = new Key();
        key.setName("key1");
        entityManager.persist(key);

        SuperUser superUser = new SuperUser();
        superUser.setName("superUser1");
        superUser.setGroups(Collections.singletonList(group));
        superUser.setKey(key);
        entityManager.persist(superUser);
    }

    private void baseUserCreate(Group group, int index, boolean withAddresses) {
        User user = new User();
        user.setName("user" + index);
        user.setGroups(Collections.singletonList(group));
        entityManager.persist(user);

        if (withAddresses) {
            Address address = new Address();
            address.setName("user" + index + "street1");
            address.setUser(user);

            Address address2 = new Address();
            address2.setName("user" + index + "street2");
            address2.setUser(user);
            entityManager.persist(address);
            entityManager.persist(address2);
        }
    }


}