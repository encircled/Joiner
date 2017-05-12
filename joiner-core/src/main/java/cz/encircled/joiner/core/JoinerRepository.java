package cz.encircled.joiner.core;

import cz.encircled.joiner.query.JoinerQuery;

import java.util.List;

/**
 * Base interface for repositories with joiner support. Can be used for repository-per-entity approach
 *
 * @author Kisel on 11.01.2016.
 */
public interface JoinerRepository<T> {

    /**
     * Find multiple objects for given parameters
     *
     * @param request request with parameters
     * @param <R>     type of result object
     * @param <U>     type of "from"
     * @return list of found objects, not null
     */
    <R, U extends T> List<R> find(JoinerQuery<U, R> request);

    /**
     * Find one object (or null) for given parameters
     *
     * @param request request with parameters
     * @param <R> type of result object
     * @param <U> type of "from"
     * @return found object or null
     *
     * @throws cz.encircled.joiner.exception.JoinerException if query returns multiple results
     */
    <R, U extends T> R findOne(JoinerQuery<U, R> request);

}
