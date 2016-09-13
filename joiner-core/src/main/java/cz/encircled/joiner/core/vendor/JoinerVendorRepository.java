package cz.encircled.joiner.core.vendor;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * Implementation is responsible for vendor-specific part of query creation logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    JPAQuery createQuery(EntityManager entityManager);

    void addJoin(JPAQuery query, JoinDescription joinDescription);

    void addFetch(JPAQuery query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath);

    <T> List<T> getResultList(JPAQuery query, Expression<T> projection);

}
