package cz.encircled.joiner;

import java.util.Collections;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import cz.encircled.joiner.config.TestConfig;
import cz.encircled.joiner.model.Group;
import cz.encircled.joiner.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Kisel on 11.01.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestConfig.class })
@Transactional
@Commit
public class AbstractTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void test() {
        System.out.println("Hey!");

        Group group = new Group();
        group.setName("testGroup");
        group.setId(1L);
        entityManager.persist(group);

        User user = new User();
        user.setId(1L);
        user.setName("test");
        entityManager.persist(user);
        System.out.println("Done!");

        user.setGroup(Collections.singletonList(group));
        entityManager.persist(user);
    }

}
