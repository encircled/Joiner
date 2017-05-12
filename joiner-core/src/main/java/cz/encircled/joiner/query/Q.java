package cz.encircled.joiner.query;

import com.mysema.query.Tuple;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.util.Assert;

/**
 * This class contains helper methods for joiner query building
 */
public class Q {

    /**
     * Build tuple query projections (i.e. select clause)
     *
     * @param returnProjections path to query projection
     * @return joiner query with custom tuple query projection
     */
    public static FromBuilder<Tuple> select(Expression<?>... returnProjections) {
        Assert.notNull(returnProjections);
        return new TupleQueryFromBuilder(returnProjections);
    }

    /**
     * Build  query projection (i.e. select clause)
     *
     * @param returnProjection path to query projection
     * @param <R>              type of source entity
     * @return joiner query with custom query projection
     */
    public static <R> FromBuilder<R> select(Expression<R> returnProjection) {
        return new ExpressionQueryFromBuilder<>(returnProjection);
    }

    /**
     * Build "from" clause of query
     *
     * @param from alias of source entity
     * @param <T> type of source entity
     * @return joiner query
     */
    public static <T> JoinerQuery<T, T> from(EntityPath<T> from) {
        return new JoinerQueryBase<>(from, from);
    }

    /**
     * Build count query
     *
     * @param from alias of source entity
     * @param <T> type of source entity
     * @return count joiner query
     */
    public static <T> JoinerQuery<T, Long> count(EntityPath<T> from) {
        JoinerQueryBase<T, Long> request = new JoinerQueryBase<>(from, true);
        request.distinct(false);
        return request;
    }

}
