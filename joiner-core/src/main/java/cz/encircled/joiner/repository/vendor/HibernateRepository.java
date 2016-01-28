package cz.encircled.joiner.repository.vendor;

import java.util.Collection;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.HQLTemplates;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.query.JoinDescription;

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
}
