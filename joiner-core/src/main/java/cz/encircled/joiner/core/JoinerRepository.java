package cz.encircled.joiner.core;

import java.util.List;

import cz.encircled.joiner.query.JoinerQuery;

/**
 * @author Kisel on 11.01.2016.
 */
public interface JoinerRepository<T> {

    <R> List<R> find(JoinerQuery<T, R> request);

    <R> R findOne(JoinerQuery<T, R> request);

}
