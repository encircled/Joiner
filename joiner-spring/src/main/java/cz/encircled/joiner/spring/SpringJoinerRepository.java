package cz.encircled.joiner.spring;

import com.mysema.query.types.Expression;
import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.core.JoinerRepository;
import cz.encircled.joiner.query.Q;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Parent class for repositories with Joiner support within spring context.
 *
 * @author Vlad on 14-Aug-16.
 */
public abstract class SpringJoinerRepository<T> implements JoinerRepository<T> {

    @Autowired
    protected Joiner delegate;

    @Override
    public List<T> find(Q<T> request) {
        return delegate.find(request);
    }

    @Override
    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        return delegate.find(request, projection);
    }

    @Override
    public T findOne(Q<T> request) {
        return delegate.findOne(request);
    }

    @Override
    public <P> P findOne(Q<T> request, Expression<P> projection) {
        return delegate.findOne(request, projection);
    }

}