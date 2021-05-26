package cz.encircled.joiner.query;

import com.querydsl.jpa.JPQLSerializer;
import com.querydsl.jpa.impl.JPAQuery;

import javax.persistence.EntityManager;

public class ExtendedJPAQuery<T> extends JPAQuery<T> {

    public ExtendedJPAQuery(EntityManager entityManager, JPAQuery<T> another) {
        super(entityManager, another.getMetadata().clone());
        clone(another);
    }

    public JPQLSerializer getSerializer() {
        return serialize(false);
    }

}
