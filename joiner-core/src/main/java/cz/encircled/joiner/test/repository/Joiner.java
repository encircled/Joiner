package cz.encircled.joiner.test.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.exception.AliasAlreadyUsedException;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.InsufficientSinglePathException;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.repository.vendor.HibernateRepository;
import cz.encircled.joiner.test.repository.vendor.JoinerVendorRepository;
import cz.encircled.joiner.util.JoinerUtil;
import org.springframework.util.Assert;

/**
 * @author Kisel on 26.01.2016.
 */
public class Joiner<T> implements QRepository<T> {

    private EntityManager entityManager;

    private EntityPath<T> rootPath;

    private JoinerVendorRepository joinerVendorRepository;

    private Set<JoinerAliasResolver> aliasResolvers;

    public Joiner(EntityManager entityManager, EntityPath<T> rootPath) {
        Assert.notNull(entityManager);
        Assert.notNull(rootPath);

        this.entityManager = entityManager;
        this.rootPath = rootPath;
        joinerVendorRepository = new HibernateRepository(); // TODO
    }

    public void setAliasResolvers(Set<JoinerAliasResolver> aliasResolvers) {
        this.aliasResolvers = aliasResolvers;
    }

    public List<T> find(Q<T> request) {
        return find(request, rootPath);
    }

    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        Assert.notNull(request);
        Assert.notNull(projection);

        JPAQuery query = joinerVendorRepository.createQuery(entityManager);
        if (request.getRootEntityPath() == null) {
            request.rootEntityPath(rootPath);
        }
        query.from(request.getRootEntityPath());
        if (request.isDistinct()) {
            query.distinct();
        }

        Set<Path<?>> usedAliases = new HashSet<Path<?>>();
        usedAliases.add(request.getRootEntityPath());

        for (JoinDescription join : request.getJoins()) {
            if (join.getAlias() == null) {
                setAliasFromResolver(join);

                if (join.getAlias() == null) {
                    setDefaultAlias(join);
                }
            }

            if (usedAliases.contains(join.getAlias())) {
                throw new AliasAlreadyUsedException("Alias " + join.getAlias() + " is already used!");
            }

            checkSinglePathCompletion(join);
            checkRootIsPresent(usedAliases, join);

            usedAliases.add(join.getAlias());

            joinerVendorRepository.addJoin(query, join);
            if (join.isFetch()) {
                joinerVendorRepository.addFetch(query, join);
            }
        }

        Predicate predicate = request.getPredicate();
        if (predicate instanceof Operation) {
            for (Object o : ((Operation) predicate).getArgs()) {
                if (o instanceof Path) {
                    Path predicatePath = ((Path) o).getRoot();
                    if (predicatePath.toString().startsWith("any(")) {
                        // TODO what to do?
                    } else {
                        if (!usedAliases.contains(predicatePath)) {
                            throw new AliasMissingException("Alias " + predicatePath + " is not present in joins!");
                        }
                    }
                }
            }
        }

        query.where(predicate);
        return query.list(projection);
    }

    private void setDefaultAlias(JoinDescription join) {
        if (join.isCollectionPath()) {
            join.alias(JoinerUtil.getDefaultAlias(join.getCollectionPath()));
        } else {
            join.alias(JoinerUtil.getDefaultAlias(join.getSinglePath()));
        }
    }

    private void setAliasFromResolver(JoinDescription join) {
        if (join.isCollectionPath()) {
            EntityPath<?> resolved = resolveCollectionAlias(join);
            if (resolved != null) {
                join.alias(resolved);
            }
        } else {

        }
    }

    private EntityPath<?> resolveCollectionAlias(JoinDescription join) {
        if (aliasResolvers != null) {
            for (JoinerAliasResolver aliasResolver : aliasResolvers) {
                EntityPath<?> resolved = aliasResolver.resolveAlias(join.getCollectionPath());
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return null;
    }

    private void checkSinglePathCompletion(JoinDescription join) {
        if (!join.isCollectionPath()) {
            if (join.getSinglePath().toString().equals(join.getSinglePath().getRoot().toString())) {
                throw new InsufficientSinglePathException(
                        "Set full path for single path join. For example 'QUser.user.address' instead of 'QAddress.address' ");
            }
        }
    }

    private void checkRootIsPresent(Set<Path<?>> usedAliases, JoinDescription join) {
        Path<?> root = join.isCollectionPath() ? join.getCollectionPath().getRoot() : join.getSinglePath().getRoot();

        if (!usedAliases.contains(root)) {
            throw new AliasMissingException("Can't join " + join + ", alias " + root + " is not present!");
        }
    }

    public T findOne(Q<T> request) {
        return null;
    }

    public <P> P findOne(Q<T> request, Expression<P> projection) {
        return null;
    }

}
