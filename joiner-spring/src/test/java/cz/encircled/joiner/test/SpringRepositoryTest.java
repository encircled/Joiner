package cz.encircled.joiner.test;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.spring.PageableFeature;
import cz.encircled.joiner.test.config.SpringTestConfig;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.model.QStatus;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import cz.encircled.joiner.test.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * @author Vlad on 14-Aug-16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SpringTestConfig.class})
@Transactional
@TestExecutionListeners(listeners = {TestDataListener.class})
public class SpringRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

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

    @Test
    public void testPagination() {
        PageableFeature feature = new PageableFeature(PageRequest.of(0, 10, Sort.by(Sort.Order.asc("statuses.name"))));

        List<User> users = userRepository.find(Q.from(QUser.user1)
                .joins(QStatus.status)
                .addFeatures(feature)
        );
    }

    @Test
    public void testFindPageWithFetchJoin() {
        Page<User> page = userRepository.findPage(Q.from(QUser.user1).joins(QGroup.group), PageRequest.of(0, 1));
        Assert.assertNotNull(page.getContent());
        Assert.assertEquals(1, page.getContent().size());
        Assert.assertEquals(entityManager.createQuery("select count(u) from User u").getSingleResult(), page.getTotalElements());
    }

    @Test
    public void testFindPageWithFetchJoinGraph() {
        Page<User> page = userRepository.findPage(Q.from(QUser.user1).joinGraphs("userGroups"), PageRequest.of(0, 1));
        Assert.assertNotNull(page.getContent());
        Assert.assertEquals(1, page.getContent().size());
        Assert.assertEquals(entityManager.createQuery("select count(u) from User u").getSingleResult(), page.getTotalElements());
    }

}
