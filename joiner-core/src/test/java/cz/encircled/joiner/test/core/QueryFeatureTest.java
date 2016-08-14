package cz.encircled.joiner.test.core;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = {TestConfig.class})
public class QueryFeatureTest extends AbstractTest {

    @Test(expected = TestException.class)
    public void testQueryFeatureBefore() {
        Q<User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T> Q<T> before(Q<T> request) {
                throw new TestException();
            }

            @Override
            public JPAQuery after(Q<?> request, JPAQuery query) {
                return query;
            }
        });
        joiner.find(request);
    }

    @Test(expected = TestException.class)
    public void testQueryFeatureAfter() {
        Q<User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T> Q<T> before(Q<T> request) {
                return request;
            }

            @Override
            public JPAQuery after(Q<?> request, JPAQuery query) {
                throw new TestException();
            }
        });
        joiner.find(request);
    }

}
