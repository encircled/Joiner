package cz.encircled.joiner.query;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    <T> Q<T> before(Q<T> request);

    JPAQuery after(Q<?> request, JPAQuery query);

}
