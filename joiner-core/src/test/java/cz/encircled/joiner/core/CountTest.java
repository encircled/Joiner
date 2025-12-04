package cz.encircled.joiner.core;

import cz.encircled.joiner.model.*;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Kisel on 31.10.2016.
 */
public abstract class CountTest extends AbstractTest {

    @Test
    public void testCountCustomProjection() {
        JoinerQuery<User, Long> query = Q.select(QUser.user1.count()).from(QUser.user1).joins(J.inner(QGroup.group));
        Long count = joiner.findOne(query);

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u inner join u.groups g").getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCount() {
        Long count = joiner.findOne(Q.count(QUser.user1).joins(J.inner(QGroup.group)));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u inner join u.groups g").getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCountNativeQuery() {
        Long count = joiner.findOne(Q.count(QUser.user1).joins(J.left(QGroup.group)).nativeQuery(true));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u left join u.groups g").getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCountFetchJoin() {
        Assertions.assertNotNull(joiner.findOne(Q.count(QUser.user1).joins(QGroup.group)));
    }

}
