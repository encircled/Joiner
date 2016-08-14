package cz.encircled.joiner.test.repository.impl;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.Joiner;
import cz.encircled.joiner.repository.JoinerRepository;
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
        setDefaultRootPath(request);
        return delegate.find(request);
    }

    @Override
    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        setDefaultRootPath(request);
        return delegate.find(request, projection);
    }

    @Override
    public T findOne(Q<T> request) {
        setDefaultRootPath(request);
        return delegate.findOne(request);
    }

    @Override
    public <P> P findOne(Q<T> request, Expression<P> projection) {
        setDefaultRootPath(request);
        return delegate.findOne(request, projection);
    }

    private void setDefaultRootPath(Q<T> request) {
        if (request != null && request.getFrom() == null) {
            request.rootEntityPath(getRootEntityPath());
        }
    }

    /**
     * Implementations may override this method to specify default root path, which is used when `from` is not set in a request
     */
    protected EntityPath<T> getRootEntityPath() {
        return null;
    }

}
