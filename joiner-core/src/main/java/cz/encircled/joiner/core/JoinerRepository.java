package cz.encircled.joiner.core;

import java.util.List;

import cz.encircled.joiner.query.JoinerQuery;

/**
 * @author Kisel on 11.01.2016.
 */
public interface JoinerRepository<T> {

    <R, U extends T> List<R> find(JoinerQuery<U, R> request);

    <R, U extends T> R findOne(JoinerQuery<U, R> request);

}
