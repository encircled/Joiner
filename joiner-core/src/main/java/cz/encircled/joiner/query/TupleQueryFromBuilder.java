package cz.encircled.joiner.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;

/**
 * @author Kisel on 13.9.2016.
 */
public class TupleQueryFromBuilder implements FromBuilder<Tuple> {

    private Expression<?>[] returnProjections;

    TupleQueryFromBuilder(Expression<?>... returnProjections) {
        this.returnProjections = returnProjections;
    }

    @Override
    public <T> JoinerQuery<T, Tuple> from(EntityPath<T> from) {
        return new TupleJoinerQuery<>(from, returnProjections);
    }

}
