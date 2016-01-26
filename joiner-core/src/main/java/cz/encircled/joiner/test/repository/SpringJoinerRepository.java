package cz.encircled.joiner.test.repository;

import java.util.Set;

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
    private Set<JoinerAliasResolver> aliasResolvers;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected Set<JoinerAliasResolver> getAliasResolvers() {
        return aliasResolvers;
    }

}
