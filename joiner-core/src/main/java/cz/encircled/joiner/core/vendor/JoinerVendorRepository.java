package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.JPQLQuery;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;

/**
 * Implementation is responsible for vendor-specific logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    <R> JPQLQuery<R> createQuery(EntityManager entityManager, JoinerProperties joinerProperties, boolean isForCount);

    void addJoin(JPQLQuery<?> query, JoinDescription joinDescription);

    void addFetch(JPQLQuery<?> query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath);

    <T> List<T> getResultList(JoinerQuery<?, T> request, JPQLQuery<T> query, JoinerProperties joinerProperties);

}
