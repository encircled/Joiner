package cz.encircled.joiner.query;

/**
 * Query features allow to modify the request/query before executing in declarative way
 *
 * @author Vlad on 27-Jul-16.
 */
public interface QueryFeature {

    /**
     * This method is called before JPA query creation and allows request modification
     *
     * @param request initial joiner request
     * @param <T>     query from
     * @param <R>     query return type
     * @return modified request
     */
    <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> request);

    /**
     * This method is called just before JPA query execution and allows to modify result query directly
     *
     * @param request initial joiner request
     * @param query   JPA query
     * @return modified JPA query to be executed
     */
    default <T, R> ExtendedJPAQuery<R> after(JoinerQuery<T, R> request, ExtendedJPAQuery<R> query) {
        return query;
    }

}
