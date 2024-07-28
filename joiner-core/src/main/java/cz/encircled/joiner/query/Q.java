package cz.encircled.joiner.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import cz.encircled.joiner.util.Assert;

/**
 * This class contains helper methods for joiner query building
 */
public class Q {

    /**
     * Build a tuple query projections (i.e. select clause)
     *
     * @param returnProjections path to query projection
     * @return joiner query with custom tuple query projection
     */
    public static FromBuilder<Tuple> select(Expression<?>... returnProjections) {
        Assert.notNull(returnProjections);
        return new TupleQueryFromBuilder(returnProjections);
    }

    /**
     * Build a query projection (i.e. select clause)
     *
     * @param returnProjection path to be selected
     * @param <R>              type of source entity
     * @return joiner query with custom query projection
     */
    public static <R> FromBuilder<R> select(Expression<R> returnProjection) {
        return new ExpressionQueryFromBuilder<>(returnProjection);
    }

    /**
     * Build a query projection (i.e. select clause) with result mapping to another object
     *
     * @param mapTo class for result to be mapped to. Must contain a constructor matching given 'returnProjections'
     * @param returnProjections paths to be selected
     * @return joiner query with custom query projection
     */
    public static <R> FromBuilder<R> select(Class<R> mapTo, Expression<?>... returnProjections) {
        return new ExpressionQueryFromBuilder<>(Projections.constructor(mapTo, returnProjections));
    }

    /**
     * Build "from" clause of query
     *
     * @param from alias of source entity
     * @param <T>  type of source entity
     * @return joiner query
     */
    public static <T> JoinerQuery<T, T> from(EntityPath<T> from) {
        return new JoinerQueryBase<>(from, from);
    }

    /**
     * Build count query
     *
     * @param from alias of source entity
     * @param <T>  type of source entity
     * @return count joiner query
     */
    public static <T> JoinerQuery<T, Long> count(EntityPath<T> from) {
        JoinerQueryBase<T, Long> request = new JoinerQueryBase<>(from, true);
        request.distinct(false);
        return request;
    }

}
