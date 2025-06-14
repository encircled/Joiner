package cz.encircled.joiner.query;

import com.querydsl.core.types.*;

/**
 * @author Kisel on 13.9.2016.
 */
public class ExpressionQueryFromBuilder<R> implements FromBuilder<R> {

    private final Expression<R> projection;

    public ExpressionQueryFromBuilder(Expression<R> projection) {
        this.projection = projection;
    }

    @Override
    public <T> JoinerQuery<T, R> from(EntityPath<T> from) {
        JoinerQueryBase<T, R> query = new JoinerQueryBase<>(from, projection);
        if (projection instanceof Operation) {
            // Check is count expression
            if (((Operation<R>) projection).getOperator().name().startsWith("COUNT_")) {
                query.count();
            };
            // 'function' projection should be non-distinct by default
            query.distinct(false);
        }
        return query;
    }

}
