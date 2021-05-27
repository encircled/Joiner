package cz.encircled.joiner.core;

import cz.encircled.joiner.config.TestConfig;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.ExtendedJPAQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class })
public class QueryFeatureTest extends AbstractTest {

    @Test
    public void testQueryFeatureBefore() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                throw new TestException();
            }

            @Override
            public <T, R> ExtendedJPAQuery<R> after(JoinerQuery<T, R> request, ExtendedJPAQuery<R> query) {
                return query;
            }
        });

        assertThrows(TestException.class, () -> joiner.find(request));
    }

    @Test
    public void testQueryFeatureCollectionBefore() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(Collections.singletonList(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                throw new TestException();
            }

            @Override
            public <T, R> ExtendedJPAQuery<R> after(JoinerQuery<T, R> request, ExtendedJPAQuery<R> query) {
                return query;
            }
        }));
        assertThrows(TestException.class, () -> joiner.find(request));
    }

    @Test
    public void testQueryFeatureAfter() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                return request;
            }

            @Override
            public <T, R> ExtendedJPAQuery<R> after(JoinerQuery<T, R> request, ExtendedJPAQuery<R> query) {
                throw new TestException();
            }
        });
        assertThrows(TestException.class, () -> joiner.find(request));
    }

}
