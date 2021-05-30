package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Kisel on 31.10.2016.
 */
public class CountTest extends AbstractTest {

    @Test
    public void testCount() {
        Long count = joiner.findOne(Q.count(QUser.user1).joins(J.inner(QGroup.group)));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u inner join u.groups").getSingleResult();

        Assertions.assertEquals(real, count);
    }

    @Test
    public void testCountFetchJoin() {
        Assertions.assertNotNull(joiner.findOne(Q.count(QUser.user1).joins(QGroup.group)));
    }

}
