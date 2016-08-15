package cz.encircled.joiner.core;

import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.Q;

import java.util.List;

/**
 * @author Kisel on 11.01.2016.
 */
public interface JoinerRepository<T> {

    List<T> find(Q<T> request);

    <P> List<P> find(Q<T> request, Expression<P> projection);

    T findOne(Q<T> request);

    <P> P findOne(Q<T> request, Expression<P> projection);

}
