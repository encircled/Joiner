package cz.encircled.joiner.query;

import com.querydsl.jpa.impl.JPAQuery;

/**
 * Query features allows to modify the request/query before executing
 *
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    /**
     * This method is called before JPA query creation and allows request modification
     *
     * @param request initial joiner request
     * @param <T> query from
     * @param <R> query return type
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
    <T, R> JPAQuery<R> after(JoinerQuery<T, R> request, JPAQuery<R> query);

}
