package cz.encircled.joiner.query;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.Expression;

/**
 * Used for subqueries in 'exists' operations
 */
public class CollectionJoinerQuery<T, R> extends JoinerQueryBase<T, R> {

    final CollectionExpression<?, T> fromCollection;

    public CollectionJoinerQuery(CollectionExpression<?, T> from, Expression<R> alias) {
        super(null, alias);
        this.fromCollection = from;
    }

    public CollectionExpression<?, T> getFromCollection() {
        return fromCollection;
    }

    @Override
    public JoinerQuery<T, R> copy() {
        return this;
    }
}
