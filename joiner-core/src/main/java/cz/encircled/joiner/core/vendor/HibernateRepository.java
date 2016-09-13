package cz.encircled.joiner.core.vendor;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.HQLTemplates;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    @Override
    public JPAQuery createQuery(EntityManager entityManager) {
        return new JPAQuery(entityManager, HQLTemplates.DEFAULT);
    }

    @Override
    public void addFetch(JPAQuery query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        query.fetch();
    }

    @Override
    public <T> List<T> getResultList(JPAQuery query, Expression<T> projection) {
        return query.list(projection);
    }
}
