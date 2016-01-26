package cz.encircled.joiner.test.repository.vendor;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.JoinDescription;

/**
 * Implementation is responsible for vendor-specific part of query creation logic
 *
 * @author Kisel on 21.01.2016.
 */
public interface JoinerVendorRepository {

    JPAQuery createQuery(EntityManager entityManager);

    void addJoin(JPAQuery query, JoinDescription joinDescription);

    void addFetch(JPAQuery query, JoinDescription joinDescription);

}
