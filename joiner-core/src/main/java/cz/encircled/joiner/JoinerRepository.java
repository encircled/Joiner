package cz.encircled.joiner;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @author Kisel on 21.01.2016.
 */
public interface JoinerRepository {

    JPAQuery createQuery(EntityManager entityManager);

    void addJoin(JPAQuery query, JoinDescription joinDescription);

    void addFetch(JPAQuery query);

}
