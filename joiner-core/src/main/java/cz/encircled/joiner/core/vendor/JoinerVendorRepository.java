package cz.encircled.joiner.core.vendor;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.query.join.JoinDescription;

import javax.persistence.EntityManager;
import java.util.Collection;

/**
 * Implementation is responsible for vendor-specific part of query creation logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    JPAQuery createQuery(EntityManager entityManager);

    void addJoin(JPAQuery query, JoinDescription joinDescription);

    void addFetch(JPAQuery query, JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath);

}
