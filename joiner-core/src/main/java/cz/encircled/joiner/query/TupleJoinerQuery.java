package cz.encircled.joiner.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.QTuple;
import cz.encircled.joiner.util.ReflectionUtils;

/**
 * Implementation of a joiner query with {@link Tuple} result
 *
 * @author Kisel on 13.9.2016.
 */
public class TupleJoinerQuery<T> extends JoinerQueryBase<T, Tuple> {

    private final Expression<?>[] returnProjections;

    public TupleJoinerQuery(EntityPath<T> from, Expression<?>... returnProjections) {
        super(from);
        this.returnProjections = returnProjections;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<Tuple> getReturnProjection() {
        return ReflectionUtils.instantiate(QTuple.class, (Object) returnProjections);
    }

}
