package cz.encircled.joiner.core.vendor;

import java.util.Collection;

import javax.persistence.EntityManager;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
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

}
