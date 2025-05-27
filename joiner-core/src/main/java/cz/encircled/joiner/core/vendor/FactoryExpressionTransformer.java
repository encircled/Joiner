package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.FactoryExpression;
import org.hibernate.query.TupleTransformer;

/**
 * {@code FactoryExpressionTransformer} is a TupleTransformer implementation using
 * FactoryExpression instances for transformation
 *
 * @author QueryDSL team
 *
 */
public final class FactoryExpressionTransformer implements TupleTransformer<Object> {

    private final transient FactoryExpression<?> projection;

    public FactoryExpressionTransformer(FactoryExpression<?> projection) {
        this.projection = projection;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        if (projection.getArgs().size() < tuple.length) {
            Object[] shortened = new Object[projection.getArgs().size()];
            System.arraycopy(tuple, 0, shortened, 0, shortened.length);
            tuple = shortened;
        }
        return projection.newInstance(tuple);
    }

}
