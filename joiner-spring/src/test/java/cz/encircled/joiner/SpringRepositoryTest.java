package cz.encircled.joiner;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.UserRepository;
import cz.encircled.joiner.spring.PageableFeature;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vlad on 14-Aug-16.
 */
public class SpringRepositoryTest extends AbstractSpringJoinerTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSpringRepository() {
        User user = new User();
        String testName = "testName";
        user.setName(testName);

        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        User found = userRepository.findOne(Q.from(QUser.user1).where(QUser.user1.name.eq(testName)));
        assertNotNull(found);
        assertEquals(testName, found.getName());
    }

    @Test
    public void testPagination() {
        PageableFeature feature = new PageableFeature(PageRequest.of(1, 2, Sort.by(Sort.Order.asc("name"))));

        List<User> users = userRepository.find(Q.from(QUser.user1)
                .where(QUser.user1.name.startsWith("user"))
                .addFeatures(feature)
        );

        assertEquals(2, users.size());
        assertEquals("user2", users.get(0).getName());
        assertEquals("user3", users.get(1).getName());
    }

    @Test
    public void testFindPageWithFetchJoin() {
        Page<User> page = userRepository.findPage(Q.from(QUser.user1).joins(QGroup.group), PageRequest.of(0, 1));
        assertNotNull(page.getContent());
        assertEquals(1, page.getContent().size());
        assertEquals(entityManager.createQuery("select count(u) from User u").getSingleResult(), page.getTotalElements());
    }

    @Test
    public void testFindPageWithFetchJoinGraph() {
        Page<User> page = userRepository.findPage(Q.from(QUser.user1).joinGraphs("userGroups"), PageRequest.of(0, 1));
        assertNotNull(page.getContent());
        assertEquals(1, page.getContent().size());
        assertEquals(entityManager.createQuery("select count(u) from User u").getSingleResult(), page.getTotalElements());
    }

}
