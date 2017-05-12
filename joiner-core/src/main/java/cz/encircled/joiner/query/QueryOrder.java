package cz.encircled.joiner.query;

import com.mysema.query.types.Expression;

/**
 * Definition of result set order
 *
 * @author Vlad on 15-Oct-16.
 */
public class QueryOrder<T> {

    private final boolean isAsc;

    private final Expression<T> target;

    public QueryOrder(boolean isAsc, Expression<T> target) {
        this.isAsc = isAsc;
        this.target = target;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public Expression<T> getTarget() {
        return target;
    }

}
