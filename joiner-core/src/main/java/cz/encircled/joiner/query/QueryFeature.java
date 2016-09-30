package cz.encircled.joiner.query;

import com.mysema.query.jpa.impl.JPAQuery;

/**
 * Query features allows to consumer to modify the request/query before executing
 *
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    /**
     * This method is called before JPA query creation and allows request modification
     *
     * @param request initial joiner request
     * @return modified request
     */
    <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> request);

    /**
     * This method is called just before JPA query execution and allows to modify result query directly
     *
     * @param request initial joiner request
     * @param query JPA query
     * @return modified JPA query to be executed
     */
    JPAQuery after(JoinerQuery<?, ?> request, JPAQuery query);

}
