package cz.encircled.joiner.repository;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import cz.encircled.joiner.alias.JoinerAliasResolver;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kisel on 26.01.2016.
 */
public abstract class SpringJoinerRepository<T> extends RepositoryParent<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired(required = false)
    private List<JoinerAliasResolver> aliasResolvers;

    @Autowired(required = false)
    private List<QueryPostProcessor> queryPostProcessors;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected List<JoinerAliasResolver> getAliasResolvers() {
        return aliasResolvers;
    }

    @Override
    protected List<QueryPostProcessor> getQueryPostProcessors() {
        return queryPostProcessors;
    }

}
