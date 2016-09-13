package cz.encircled.joiner.query;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> request);

    JPAQuery after(JoinerQuery<?, ?> request, JPAQuery query);

}
