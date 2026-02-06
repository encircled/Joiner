package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.UserRole;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class EnumTest extends AbstractTest {

    @Test
    public void testCountNativeQueryWithEnumTypeString() {
        Long count = joiner.findOne(Q.count(QUser.user1).where(QUser.user1.userRole.eq(UserRole.ADMIN)).nativeQuery(true));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u where u.userRole = :role")
                .setParameter("role", UserRole.ADMIN)
                .getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCountQueryWithEnumTypeString() {
        Long count = joiner.findOne(Q.count(QUser.user1).where(QUser.user1.userRole.eq(UserRole.ADMIN)));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u where u.userRole = :role")
                .setParameter("role", UserRole.ADMIN)
                .getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCountNativeQueryWithEnumTypeOrdinal() {
        Long count = joiner.findOne(Q.count(QUser.user1).where(QUser.user1.secondaryRole.eq(UserRole.USER)).nativeQuery(true));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u where u.secondaryRole = :role")
                .setParameter("role", UserRole.USER)
                .getSingleResult();

        assertEquals(real, count);
    }

    @Test
    public void testCountQueryWithEnumTypeOrdinal() {
        Long count = joiner.findOne(Q.count(QUser.user1).where(QUser.user1.secondaryRole.eq(UserRole.USER)));

        entityManager.clear();

        Long real = (Long) entityManager.createQuery("select count(u) from User u where u.secondaryRole = :role")
                .setParameter("role", UserRole.USER)
                .getSingleResult();

        assertEquals(real, count);
    }
}
