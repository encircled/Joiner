package cz.encircled.joiner.test;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.SpringTestConfig;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import cz.encircled.joiner.test.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Vlad on 14-Aug-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringTestConfig.class})
@Transactional
public class SpringRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testSpringRepository() {
        User user = new User();
        String testName = "testName";
        user.setName(testName);

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findOne(Q.from(QUser.user1).where(QUser.user1.name.eq(testName)));
        Assert.assertNotNull(found);
        Assert.assertEquals(testName, found.getName());
    }

}
