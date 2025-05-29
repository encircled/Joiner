package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.*;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.List;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends VendorRepository {

    @Override
    public JoinerJpaQuery createQuery(JoinerQuery<?, ?> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request);

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

}
