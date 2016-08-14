package cz.encircled.joiner.repository;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.Q;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Kisel on 21.01.2016.
 */
public abstract class JoinerRepository<T> implements QRepository<T> {

    protected Joiner delegate;

    @Override
    public List<T> find(Q<T> request) {
        checkRootPath(request);
        return delegate.find(request);
    }

    @Override
    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        checkRootPath(request);
        return delegate.find(request, projection);
    }

    @Override
    public T findOne(Q<T> request) {
        checkRootPath(request);
        return delegate.findOne(request);
    }

    @Override
    public <P> P findOne(Q<T> request, Expression<P> projection) {
        checkRootPath(request);
        return delegate.findOne(request, projection);
    }

    @PostConstruct
    private void init() {
        delegate = new Joiner(getEntityManager());
    }

    private void checkRootPath(Q<T> request) {
        if (request != null && request.getRootEntityPath() == null) {
            request.rootEntityPath(getRootEntityPath());
        }
    }

    protected abstract EntityManager getEntityManager();

    protected abstract EntityPath<T> getRootEntityPath();

}
