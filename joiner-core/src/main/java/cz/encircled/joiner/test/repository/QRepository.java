package cz.encircled.joiner.test.repository;

import java.util.List;

import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.Q;

/**
 * @author Kisel on 11.01.2016.
 */
public interface QRepository<T> {

    List<T> find(Q<T> request);

    <P> List<P> find(Q<T> request, Expression<P> projection);

    T findOne(Q<T> request);

    <P> P findOne(Q<T> request, Expression<P> projection);

}
