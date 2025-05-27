package cz.encircled.joiner.core.hibernate;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import jakarta.persistence.FlushModeType;
import org.hibernate.query.Query;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HibernateSpecificFeaturesTest extends AbstractTest {

    @Test
    public void testHibernateQueryFeatures() {
        Query<?> query = (Query<?>) joiner.toJPAQuery(Q.from(QUser.user1).cacheable(true).cacheRegion("Test").timeout(100).flushMode(FlushModeType.COMMIT)).jpaQuery;
        Assertions.assertEquals("Test", query.getCacheRegion());
        Assertions.assertEquals(100, query.getTimeout());
        Assertions.assertTrue(query.isCacheable());
    }

}
