package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.hibernate.HibernateQuery;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.query.Query;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    private static StatelessSession statelessSession;

    private static synchronized StatelessSession getStatelessSession(EntityManager entityManager) {
        if (statelessSession == null || !statelessSession.isOpen()) {
            statelessSession = entityManager.unwrap(Session.class).getSessionFactory().openStatelessSession();
        }
        return statelessSession;
    }

    @Override
    public void addFetch(JPQLQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        query.fetchJoin();
    }

    @Override
    public <T> List<T> getResultList(JoinerQuery<?, T> request, JPQLQuery<T> query, Expression<T> projection) {
        if (query instanceof HibernateQuery) {
            Query<T> jpaQuery = ((HibernateQuery<T>) query).createQuery();
            for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
                for (Object value : entry.getValue()) {
                    jpaQuery.setHint(entry.getKey(), value);
                }
            }
            return jpaQuery.getResultList();
        }
        return super.getResultList(request, query, projection);
    }

    @Override
    public <R> JPQLQuery<R> createQuery(EntityManager entityManager, boolean useStatelessSessions) {
        if (useStatelessSessions) {
            StatelessSession session = getStatelessSession(entityManager);
            return new HibernateQuery<>(session);
        }

        return new JPAQuery<>(entityManager, HQLTemplates.DEFAULT);
    }

}
