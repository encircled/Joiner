package cz.encircled.joiner.query;

import java.lang.reflect.Field;

import com.mysema.query.Tuple;
import com.mysema.query.jpa.JPAQueryBase;
import com.mysema.query.jpa.JPAQueryMixin;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.util.ReflectionUtils;

/**
 * @author Kisel on 13.9.2016.
 */
public class TupleJoinerQuery<T> extends JoinerQueryBase<T, Tuple> {

    private static final Field queryMixinField = ReflectionUtils.findField(JPAQueryBase.class, "queryMixin");

    private final EntityPath<?>[] returnProjections;

    public TupleJoinerQuery(EntityPath<T> from, EntityPath<?>... returnProjections) {
        super(from);
        this.returnProjections = returnProjections;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Expression<Tuple> getReturnProjection(final JPAQuery query) {
        return ((JPAQueryMixin) ReflectionUtils.getField(queryMixinField, query)).createProjection(returnProjections);
    }

}
