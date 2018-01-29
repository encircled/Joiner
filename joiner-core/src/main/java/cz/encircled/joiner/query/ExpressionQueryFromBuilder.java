package cz.encircled.joiner.query;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

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
        return new JoinerQueryBase<T, R>(from, projection);
    }

}
