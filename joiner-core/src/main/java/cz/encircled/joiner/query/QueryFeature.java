package cz.encircled.joiner.query;

import com.querydsl.jpa.JPQLQuery;

import java.util.List;

/**
 * Query features allow to modify the request/query before executing in declarative way
 *
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    /**
     * This method is invoked before a JPA query creation and allows the request modification
     *
     * @param request initial joiner request
     * @param <T>     query from
     * @param <R>     query return type
     * @return modified request
     */
    default <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> request) {
        return request;
    }

    /**
     * This method is invoked just before a JPA query execution and allows to modify the result query directly
     *
     * @param request initial joiner request
     * @param query   JPA query
     * @return modified JPA query to be executed
     */
    default <T, R> JPQLQuery<R> after(JoinerQuery<T, R> request, JPQLQuery<R> query) {
        return query;
    }

    /**
     * This method is invoked after the result set is fetched, similar to JPA @PostLoad
     *
     * @param request initial joiner request
     * @param <T>     query from
     * @param <R>     query return type
     */
    default <T, R> void postLoad(JoinerQuery<T, R> request, List<R> result) {}

}
