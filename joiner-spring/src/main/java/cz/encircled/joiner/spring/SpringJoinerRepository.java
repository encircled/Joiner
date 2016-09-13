package cz.encircled.joiner.spring;

import java.util.List;

import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.core.JoinerRepository;
import cz.encircled.joiner.query.JoinerQuery;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Parent class for repositories with Joiner support within spring context.
 *
 * @author Vlad on 14-Aug-16.
 */
public abstract class SpringJoinerRepository<T> implements JoinerRepository<T> {

    @Autowired
    protected Joiner delegate;

    @Override
    public <R> List<R> find(JoinerQuery<T, R> request) {
        return delegate.find(request);
    }

    @Override
    public <R> R findOne(JoinerQuery<T, R> request) {
        return delegate.findOne(request);
    }

}