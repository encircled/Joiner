package cz.encircled.joiner.core.resolver;

import com.querydsl.core.types.Path;
import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.core.DefaultAliasResolver;
import cz.encircled.joiner.model.QAddress;
import cz.encircled.joiner.model.QKey;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Vlad on 18-Dec-16.
 */
public class AliasResolverCacheTest extends AbstractTest {

    @Test
    public void testCacheAttributeOnEntity() {
        CountingAliasResolver resolver = new CountingAliasResolver(entityManager);
        resolver.resolveFieldPathForJoinAlias(J.left(QUser.user1), QAddress.address);
        resolver.resolveFieldPathForJoinAlias(J.left(QUser.user1), QAddress.address);
        JoinDescription left = J.left(QUser.user1);
        resolver.resolveFieldPathForJoinAlias(left, QAddress.address);

        assertNotNull(left.getSinglePath());
        assertEquals(1, resolver.getCounter());
    }

    @Test
    public void testCacheAttributeOnChildEntity() {
        CountingAliasResolver resolver = new CountingAliasResolver(entityManager);
        resolver.resolveFieldPathForJoinAlias(J.left(QKey.key), QUser.user1);
        resolver.resolveFieldPathForJoinAlias(J.left(QKey.key), QUser.user1);
        JoinDescription left = J.left(QKey.key);
        resolver.resolveFieldPathForJoinAlias(left, QUser.user1);

        assertNotNull(left.getSinglePath());
        assertEquals(1, resolver.getCounter());
    }

    @Test
    public void testCacheAttributeOnParentEntity() {
        CountingAliasResolver resolver = new CountingAliasResolver(entityManager);
        resolver.resolveFieldPathForJoinAlias(J.left(QSuperUser.superUser), QAddress.address);
        resolver.resolveFieldPathForJoinAlias(J.left(QSuperUser.superUser), QAddress.address);

        JoinDescription left = J.left(QSuperUser.superUser);
        resolver.resolveFieldPathForJoinAlias(left, QAddress.address);

        assertNotNull(left.getSinglePath());
        assertEquals(1, resolver.getCounter());
    }

    @Test
    public void testCacheAttributeSubtype() {
        CountingAliasResolver resolver = new CountingAliasResolver(entityManager);
        resolver.resolveFieldPathForJoinAlias(J.left(QAddress.address), QSuperUser.superUser);
        resolver.resolveFieldPathForJoinAlias(J.left(QAddress.address), QSuperUser.superUser);
        JoinDescription left = J.left(QAddress.address);
        resolver.resolveFieldPathForJoinAlias(left, QSuperUser.superUser);

        assertNotNull(left.getCollectionPath());
        assertEquals(1, resolver.getCounter());
    }

    private static class CountingAliasResolver extends DefaultAliasResolver {

        private final AtomicInteger counter = new AtomicInteger(0);

        CountingAliasResolver(EntityManager entityManager) {
            super(entityManager);
        }

        @Override
        protected Path<?> doFindPathOnParent(Path<?> parent, Class<?> targetType, JoinDescription joinDescription) {
            counter.incrementAndGet();
            return super.doFindPathOnParent(parent, targetType, joinDescription);
        }

        int getCounter() {
            return counter.get();
        }

    }

}
