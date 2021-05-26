package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.HQLTemplates;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.ExtendedJPAQuery;
import cz.encircled.joiner.query.join.JoinDescription;

import javax.persistence.EntityManager;
import java.util.Collection;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository extends AbstractVendorRepository implements JoinerVendorRepository {

    @Override
    public <R> ExtendedJPAQuery<R> createQuery(EntityManager entityManager) {
        return new ExtendedJPAQuery<>(entityManager, new JPAQuery<>(entityManager, HQLTemplates.DEFAULT));
    }

    @Override
    public void addFetch(JPAQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath) {
        query.fetchJoin();
    }

}
