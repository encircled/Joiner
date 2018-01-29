package cz.encircled.joiner.test.core;

import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class })
public class QueryFeatureTest extends AbstractTest {

    @Test(expected = TestException.class)
    public void testQueryFeatureBefore() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                throw new TestException();
            }

            @Override
            public JPAQuery after(final JoinerQuery<?, ?> request, final JPAQuery query) {
                return query;
            }
        });
        joiner.find(request);
    }

    @Test(expected = TestException.class)
    public void testQueryFeatureCollectionBefore() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(Collections.singletonList(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                throw new TestException();
            }

            @Override
            public JPAQuery after(final JoinerQuery<?, ?> request, final JPAQuery query) {
                return query;
            }
        }));
        joiner.find(request);
    }

    @Test(expected = TestException.class)
    public void testQueryFeatureAfter() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                return request;
            }

            @Override
            public JPAQuery after(final JoinerQuery<?, ?> request, final JPAQuery query) {
                throw new TestException();
            }
        });
        joiner.find(request);
    }

}
