package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kisel on 01.02.2016.
 */
public abstract class QueryFeatureTest extends AbstractTest {

    @Test
    public void testQueryFeatureBefore() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> JoinerQuery<T, R> before(final JoinerQuery<T, R> request) {
                throw new TestException();
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
        }));
        assertThrows(TestException.class, () -> joiner.find(request));
    }

    @Test
    public void testQueryFeatureAfter() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        request.addFeatures(new QueryFeature() {
            @Override
            public <T, R> Query after(JoinerQuery<T, R> request, Query query) {
                throw new TestException();
            }
        });
        assertThrows(TestException.class, () -> joiner.find(request));
    }

    @Test
    public void testQueryFeaturePostLoad() {
        JoinerQuery<User, User> request = Q.from(QUser.user1).addFeatures(new QueryFeature() {
            @Override
            public <T, R> void postLoad(JoinerQuery<T, R> request, List<R> result) {
                throw new TestException();
            }
        });
        assertThrows(TestException.class, () -> joiner.find(request));
    }

    @Test
    public void testDefaultQueryFeature() {
        try {
            QueryFeature feature = new QueryFeature() {
                @Override
                public <T, R> void postLoad(JoinerQuery<T, R> request, List<R> result) {
                    throw new TestException();
                }
            };
            joiner.setJoinerProperties(new JoinerProperties().addDefaultQueryFeature(feature));

            assertThrows(TestException.class, () -> joiner.find(Q.from(QUser.user1)));

            joiner.getJoinerProperties().removeDefaultQueryFeature(feature);
            joiner.find(Q.from(QUser.user1));
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

}
