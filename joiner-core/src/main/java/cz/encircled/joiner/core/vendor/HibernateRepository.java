package cz.encircled.joiner.core.vendor;

import com.querydsl.core.QueryModifiers;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.FactoryExpressionTransformer;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    @Override
    public void addFetch(JPQLQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        query.fetchJoin();
    }

    @Override
    public <T> List<T> getResultList(JoinerQuery<?, T> request, JPQLQuery<T> query, JoinerProperties joinerProperties) {
        if (query instanceof HibernateQueryWithSession<T> hq) {
            try (hq.session) {
                Query<T> jpaQuery = hq.createQuery();
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
                return jpaQuery.getResultList();
            }
        }
        return super.getResultList(request, query, joinerProperties);
    }

    @Override
    public <R> JPQLQuery<R> createQuery(JoinerQuery<?, R> request, EntityManager entityManager, JoinerProperties joinerProperties) {
        boolean isStateless = request.isStatelessSession() != null ? request.isStatelessSession() : joinerProperties.useStatelessSessions;
        if (isStateless) {
            request.setStatelessSession(true);
            StatelessSession session = entityManager.unwrap(Session.class).getSessionFactory().openStatelessSession();
            return new HibernateQueryWithSession<>(session);
        }

        return new JPAQuery<>(entityManager, HQLTemplates.DEFAULT);
    }

    static class HibernateQueryWithSession<T> extends HibernateQuery<T> {
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
            JPQLSerializer serializer = serialize(forCount);
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
    }

}
