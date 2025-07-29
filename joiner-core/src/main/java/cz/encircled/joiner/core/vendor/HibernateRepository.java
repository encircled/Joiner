package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.*;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.hibernate.*;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends VendorRepository {

    private static final Logger log = LoggerFactory.getLogger(HibernateRepository.class);

    @Override
    public JoinerJpaQuery createQuery(JoinerQuery<?, ?> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request);

        if (joinerProperties.printQueries) {
            log.info("Joiner:\n {}", queryString);
        } else {
            log.debug("Joiner:\n {}", queryString);
        }

        StatelessSession session = null;
        Query<?> jpaQuery;
        boolean isStateless = request.isStatelessSession() != null ? request.isStatelessSession() : joinerProperties.useStatelessSessions;
        if (isStateless) {
            request.setStatelessSession(true);
            session = entityManager.unwrap(Session.class).getSessionFactory().openStatelessSession();
            jpaQuery = session.createQuery(queryString, null);
        } else {
            jpaQuery = (Query<?>) entityManager.createQuery(queryString);
        }

        setQueryParams(serializer, jpaQuery, request, joinerProperties);

        if (request.getCacheable() != null) {
            jpaQuery.setCacheable(request.getCacheable());
        }
        if (request.getCacheRegion() != null) {
            jpaQuery.setCacheRegion(request.getCacheRegion());
        }
        if (request.getTimeout() != null) {
            jpaQuery.setTimeout(request.getTimeout());
        }

        if (!request.isCount() && request.getReturnProjection() instanceof FactoryExpression<?> p) {
            jpaQuery.setTupleTransformer(new FactoryExpressionTransformer(p));
        }

        return new JoinerJpaQuery(jpaQuery, queryString, session);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> fetchResult(JoinerQuery<?, T> request, jakarta.persistence.Query jpaQuery) {
        return jpaQuery.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Stream<T> streamResult(JoinerQuery<?, T> request, jakarta.persistence.Query jpaQuery) {
        SelectionQuery<T> query = (SelectionQuery<T>) jpaQuery;
        query.setHibernateFlushMode(FlushMode.MANUAL);
        query.setCacheMode(CacheMode.IGNORE);
        ScrollableResults<T> results = query.scroll(ScrollMode.FORWARD_ONLY);
        Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(
                new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return results.next();
                    }

                    @Override
                    public T next() {
                        return results.get();
                    }
                },
                Spliterator.ORDERED
        );

        return StreamSupport.stream(spliterator, false).onClose(results::close);
    }
}
