package cz.encircled.joiner.spring;


import java.util.Optional;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyPath;

/**
 * This feature allows applying spring {@link Pageable} criteria (limit, offset, sort) to the query
 *
 * @author Kisel on 29.10.2016.
 */
public class PageableFeature implements QueryFeature {

    @Autowired
    private final Pageable pageable;

    public PageableFeature(Pageable pageable) {
        this.pageable = pageable;
    }

    @Override
    public <T, R> JoinerQuery<T, R> before(JoinerQuery<T, R> joinerQuery) {
        if (pageable != null) {
            joinerQuery.limit((long) pageable.getPageSize());
            joinerQuery.offset(pageable.getOffset());
            Sort sort = Optional.ofNullable(pageable.getSort()).orElse(Sort.unsorted());
            if (!sort.equals(Sort.unsorted())) {
                sort.forEach(order -> {
                    if (order.getDirection().equals(Sort.Direction.ASC)) {
                        joinerQuery.asc(buildOrderPropertyPathFrom(joinerQuery, order));
                    } else {
                        joinerQuery.desc(buildOrderPropertyPathFrom(joinerQuery, order));
                    }
                });
            }
        }
        return joinerQuery;
    }

    @Override
    public <T, R> JPAQuery<R> after(JoinerQuery<T, R> request, JPAQuery<R> query) {
        return query;
    }

    private Expression<?> buildOrderPropertyPathFrom(JoinerQuery<?, ?> joinerQuery, Sort.Order order) {
        PathBuilder<?> builder = new PathBuilder<Object>(joinerQuery.getFrom().getType(), joinerQuery.getFrom().toString());

        PropertyPath path = PropertyPath.from(order.getProperty(), builder.getType());
        Expression<?> sortPropertyExpression = builder;

        while (path != null) {
            if (!path.hasNext() && order.isIgnoreCase()) {
                // if order is ignore-case we have to treat the last path segment as a String.
                sortPropertyExpression = Expressions.stringPath((Path<?>) sortPropertyExpression, path.getSegment()).lower();
            } else {
                sortPropertyExpression = Expressions.path(path.getType(), (Path<?>) sortPropertyExpression, path.getSegment());
            }

            path = path.next();
        }

        return sortPropertyExpression;
    }

}
