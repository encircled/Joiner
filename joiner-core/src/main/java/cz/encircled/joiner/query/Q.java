package cz.encircled.joiner.query;

import com.mysema.query.Tuple;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.util.Assert;

public class Q {

    public static FromBuilder<Tuple> select(Expression<?>... returnProjections) {
        Assert.notNull(returnProjections);
        return new TupleQueryFromBuilder(returnProjections);
    }

    public static <R> FromBuilder<R> select(Expression<R> returnProjection) {
        return new ExpressionQueryFromBuilder<>(returnProjection);
    }

    public static <T> JoinerQuery<T, T> from(EntityPath<T> from) {
        return new JoinerQueryBase<>(from, from);
    }

}
