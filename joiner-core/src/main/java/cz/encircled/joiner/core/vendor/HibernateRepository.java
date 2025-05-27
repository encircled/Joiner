package cz.encircled.joiner.core.vendor;

import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.*;
import com.querydsl.jpa.FactoryExpressionTransformer;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import com.querydsl.jpa.hibernate.HibernateQuery;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends VendorRepository {

    private static final Logger log = LoggerFactory.getLogger(HibernateRepository.class);

    @Override
    public JoinerJpaQuery createQuery(JoinerQuery<?, ?> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request, request.isCount());

        StatelessSession session = null;
        Query<?> jpaQuery;
        boolean isStateless = request.isStatelessSession() != null ? request.isStatelessSession() : joinerProperties.useStatelessSessions;
        if (isStateless) {
            request.setStatelessSession(true);
            session = entityManager.unwrap(Session.class).getSessionFactory().openStatelessSession();

            jpaQuery = session.createQuery(queryString);
            for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
                for (Object value : entry.getValue()) {
                    jpaQuery.setHint(entry.getKey(), value);
                }
            }
            for (Map.Entry<String, List<Object>> entry : joinerProperties.defaultHints.entrySet()) {
                for (Object value : entry.getValue()) {
                    jpaQuery.setHint(entry.getKey(), value);
                }
            }

        } else {
            jpaQuery = (Query<?>) entityManager.createQuery(queryString);
        }

        setQueryParams(serializer, jpaQuery, request, joinerProperties);

        if (request.getReturnProjection() instanceof FactoryExpression<?> p) {
            jpaQuery.setResultTransformer(new FactoryExpressionTransformer(p));
        }

        return new JoinerJpaQuery(jpaQuery, queryString, session);
    }

    @Override
    public <T> List<T> fetchResult(JoinerQuery<?, T> request, jakarta.persistence.Query jpaQuery) {
        return jpaQuery.getResultList();
    }

    @Override
    public <T> List<T> getResultList(JoinerQuery<?, T> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request, request.isCount());
        log.debug("Joiner query: {}", queryString);

        boolean isStateless = request.isStatelessSession() != null ? request.isStatelessSession() : joinerProperties.useStatelessSessions;
        jakarta.persistence.Query jpaQuery;
        if (isStateless) {
            request.setStatelessSession(true);
            try (StatelessSession session = entityManager.unwrap(Session.class).getSessionFactory().openStatelessSession()) {
                jpaQuery = session.createQuery(queryString, Object.class);
                for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
                    for (Object value : entry.getValue()) {
                        jpaQuery.setHint(entry.getKey(), value);
                    }
                }
                for (Map.Entry<String, List<Object>> entry : joinerProperties.defaultHints.entrySet()) {
                    for (Object value : entry.getValue()) {
                        jpaQuery.setHint(entry.getKey(), value);
                    }
                }

                setQueryParams(serializer, jpaQuery, request, joinerProperties);

                if (request.getReturnProjection() instanceof FactoryExpression<?> p) {
                    ((Query) jpaQuery).setResultTransformer(new FactoryExpressionTransformer(p));
                }

                return jpaQuery.getResultList();
            }
        } else {
            jpaQuery = entityManager.createQuery(queryString);
        }

        setQueryParams(serializer, jpaQuery, request, joinerProperties);

        if (request.getReturnProjection() instanceof FactoryExpression<?> p) {
            ((Query) jpaQuery).setResultTransformer(new FactoryExpressionTransformer(p));
        }

        return jpaQuery.getResultList();
    }

    /*static class HibernateQueryWithSession<T> extends HibernateQuery<T> {
        final StatelessSession session;

        HibernateQueryWithSession(StatelessSession session) {
            super(session);
            this.session = session;
        }

        @Override
        public Query createQuery() {
            return doCreateQuery(getMetadata().getModifiers(), false);
        }

        protected Query doCreateQuery(QueryModifiers modifiers, boolean forCount) {
            JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
            serializer.serialize(getMetadata(), forCount, null);
            String queryString = serializer.toString();
            logQuery(queryString);
            Query query = session.createQuery(queryString);
            List<Object> constants = serializer.getConstants();

            for (int i = 0; i < constants.size(); i++) {
                Object val = constants.get(i);
                if (val instanceof Collection<?>) {
                    query.setParameterList(i + 1, (Collection<?>) val);
                } else {
                    query.setParameter(i + 1, val);
                }
            }
            if (timeout > 0) {
                query.setTimeout(timeout);
            }
            if (cacheable != null) {
                query.setCacheable(cacheable);
            }
            if (cacheRegion != null) {
                query.setCacheRegion(cacheRegion);
            }
            if (comment != null) {
                query.setComment(comment);
            }
            if (readOnly != null) {
                query.setReadOnly(readOnly);
            }
            for (Map.Entry<Path<?>, LockMode> entry : lockModes.entrySet()) {
                query.setLockMode(entry.getKey().toString(), entry.getValue());
            }
            if (flushMode != null) {
                if (flushMode == FlushMode.AUTO) {
                    query.setFlushMode(FlushModeType.AUTO);
                } else {
                    query.setFlushMode(FlushModeType.COMMIT);
                }
            }

            if (modifiers != null && modifiers.isRestricting()) {
                Integer limit = modifiers.getLimitAsInteger();
                Integer offset = modifiers.getOffsetAsInteger();
                if (limit != null) {
                    query.setMaxResults(limit);
                }
                if (offset != null) {
                    query.setFirstResult(offset);
                }
            }

            // set transformer, if necessary
            Expression<?> projection = getMetadata().getProjection();
            if (!forCount && projection instanceof FactoryExpression) {
                query.setResultTransformer(new FactoryExpressionTransformer((FactoryExpression<?>) projection));
            }
            return query;
        }
    }*/

}
