package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.ExtendedJPAQuery;
import cz.encircled.joiner.query.join.JoinDescription;

import javax.persistence.EntityManager;
import java.util.Collection;
import java.util.List;

/**
 * Implementation is responsible for vendor-specific logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    <R> ExtendedJPAQuery<R> createQuery(EntityManager entityManager);

    void addJoin(JPAQuery<?> query, JoinDescription joinDescription);

    void addFetch(JPAQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath);

    <T> List<T> getResultList(JPAQuery<T> query, Expression<T> projection);

}
