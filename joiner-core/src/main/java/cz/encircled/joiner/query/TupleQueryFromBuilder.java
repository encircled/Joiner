package cz.encircled.joiner.query;

import com.mysema.query.Tuple;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;

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
