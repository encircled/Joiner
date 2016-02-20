package cz.encircled.joiner.repository;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.query.Q;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

/**
 * @author Kisel on 21.01.2016.
 */
public abstract class JoinerRepository<T> implements QRepository<T> {

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
        joiner.setPostProcessors(getQueryPostProcessors());
        delegate = joiner;
    }

    protected abstract EntityManager getEntityManager();

    protected List<JoinerAliasResolver> getAliasResolvers() {
        return Collections.emptyList();
    }

    protected List<QueryPostProcessor> getQueryPostProcessors() {
        return Collections.emptyList();
    }

    protected abstract EntityPath<T> getRootEntityPath();

}
