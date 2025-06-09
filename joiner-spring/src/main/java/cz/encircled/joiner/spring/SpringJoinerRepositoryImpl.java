package cz.encircled.joiner.spring;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.SimpleExpression;
import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Parent class for entity repositories with Joiner support within spring context.
 * <p>
 * Instance of {@link Joiner} must be available in context. {@link JoinerConfiguration} can be used
 * </p>
 *
 * @author Vlad on 14-Aug-16.
 */
public abstract class SpringJoinerRepositoryImpl<T> implements SpringJoinerRepository<T> {

    @Autowired
    protected Joiner delegate;

    @Override
    public <R, U extends T> List<R> find(JoinerQuery<U, R> request) {
        return delegate.find(request);
    }

    @Override
    public <R, U extends T> Page<R> findPage(JoinerQuery<U, R> request, Pageable pageable) {
        Assert.notNull(request);
        Assert.notNull(pageable);

        Long count = getTotalCount(request);

        SimpleExpression idField = (SimpleExpression) ReflectionUtils.getField("id", request.getFrom());

        List<?> matchingIds = findMatchingIds(request, pageable, idField);

        List<R> content = delegate.find(request.copy()
                .where(idField.in(matchingIds))
                .addFeatures(new PageableFeature(pageable, true))
        );

        return new PageImpl<>(content, pageable, count);
    }

    private List findMatchingIds(JoinerQuery<?, ?> request, Pageable pageable, SimpleExpression<?> idField) {
        ArrayList<Expression<?>> newProjection = new ArrayList<>();
        newProjection.add(idField);
        newProjection.addAll(PageableFeature.getExpressionsForSortParam(request, pageable.getSort()));

        JoinerQuery<?, Tuple> copy = request.copy(newProjection.toArray(new Expression[0])).addFeatures(new PageableFeature(pageable));

        return delegate.find(copy).stream().map(t -> t.get(idField)).toList();
    }

    @Override
    public <R, U extends T> R findOne(JoinerQuery<U, R> request) {
        return delegate.findOne(request);
    }

    private <R, U extends T> Long getTotalCount(JoinerQuery<U, R> request) {
        JoinerQueryBase<?, Long> countRequest = (JoinerQueryBase<?, Long>) request.copy();
        countRequest.count();

        // Fetch is not allowed for count queries
        J.unrollChildrenJoins(countRequest.getJoins()).forEach(j -> j.fetch(false));

        return delegate.findOne(countRequest);
    }

}