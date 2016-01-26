package cz.encircled.joiner.test.repository;

import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.query.Q;

/**
 * @author Kisel on 21.01.2016.
 */
public abstract class RepositoryParent<T> implements QRepository<T> {

    protected QRepository<T> delegate;

    public List<T> find(Q<T> request) {
        return delegate.find(request);
    }

    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        return delegate.find(request, projection);
    }

    public T findOne(Q<T> request) {
        return delegate.findOne(request);
    }

    public <P> P findOne(Q<T> request, Expression<P> projection) {
        return delegate.findOne(request, projection);
    }

    @PostConstruct
    private void init() {
        Joiner<T> joiner = new Joiner<T>(getEntityManager(), getRootEntityPath());
        joiner.setAliasResolvers(getAliasResolvers());
        delegate = joiner;
    }

    protected abstract EntityManager getEntityManager();

    protected abstract Set<JoinerAliasResolver> getAliasResolvers();

    protected abstract EntityPath<T> getRootEntityPath();

}
