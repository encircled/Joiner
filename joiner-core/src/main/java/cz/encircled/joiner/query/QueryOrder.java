package cz.encircled.joiner.query;

import com.querydsl.core.types.Expression;

import java.util.Objects;

/**
 * Definition of result set order
 *
 * @author Vlad on 15-Oct-16.
 */
public class QueryOrder<T> {

    private final boolean isAsc;

    private Expression<T> target;

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

    public void setTarget(Expression<T> target) {
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof QueryOrder)) return false;
        QueryOrder<?> that = (QueryOrder<?>) o;
        return isAsc == that.isAsc &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {

        return Objects.hash(isAsc, target);
    }
}
