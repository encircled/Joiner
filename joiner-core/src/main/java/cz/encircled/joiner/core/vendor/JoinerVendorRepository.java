package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import com.querydsl.jpa.JPQLQuery;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;

/**
 * Implementation is responsible for vendor-specific logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    default void addFetch(JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath, JoinerQuery<?, ?> request) {}

    <T> List<T> getResultList(JoinerQuery<?, T> request, JoinerProperties joinerProperties, EntityManager entityManager);

}
