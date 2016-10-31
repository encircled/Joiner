package cz.encircled.joiner.spring;

import java.util.List;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.JoinerQueryBase;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Parent class for repositories with Joiner support within spring context.
 *
 * @author Vlad on 14-Aug-16.
 */
public abstract class SpringJoinerRepositoryImpl<T> implements SpringJoinerRepository<T> {

    @Autowired
    protected Joiner delegate;

    @Override
    public <R> List<R> find(JoinerQuery<T, R> request) {
        return delegate.find(request);
    }

    @Override
    public <R> Page<R> findPage(JoinerQuery<T, R> request, Pageable pageable) {
        Assert.notNull(request);
        Assert.notNull(pageable);

        Long count = getTotalCount(request);

        List<R> content = delegate.find(request.addFeatures(new PageableFeature(pageable)));

        return new PageImpl<>(content, pageable, count);
    }

    @Override
    public <R> R findOne(JoinerQuery<T, R> request) {
        return delegate.findOne(request);
    }

    private <R> Long getTotalCount(JoinerQuery<T, R> request) {
        JoinerQueryBase<?, Long> countRequest = (JoinerQueryBase) request.copy();
        countRequest.count();

        // Fetch is not allowed for count queries
        J.unrollChildrenJoins(countRequest.getJoins()).forEach(j -> j.fetch(false));

        return delegate.findOne(countRequest);
    }

}