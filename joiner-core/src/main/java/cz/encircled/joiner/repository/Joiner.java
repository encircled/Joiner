package cz.encircled.joiner.repository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.criteria.JoinType;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.exception.AliasAlreadyUsedException;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.InsufficientSinglePathException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.vendor.HibernateRepository;
import cz.encircled.joiner.repository.vendor.JoinerVendorRepository;
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
        this.joinerVendorRepository = new HibernateRepository(); // TODO
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

        addJoins(request, query, usedAliases);

        checkAliasesArePresent(request, usedAliases);

        query.where(request.getWhere());
        if (request.getGroupBy() != null) {
            query.groupBy(request.getGroupBy());
        }
        if (request.getHaving() != null) {
            query.having(request.getHaving());
        }
        return query.list(projection);
    }

    private void addJoins(Q<T> request, JPAQuery query, Set<Path<?>> usedAliases) {
        for (JoinDescription join : request.getJoins()) {
            resolveJoinAlias(usedAliases, join);

            checkSinglePathCompletion(join);
            checkRootIsPresent(usedAliases, join);

            usedAliases.add(join.getAlias());

            joinerVendorRepository.addJoin(query, join);
            if (join.isFetch()) {
                if (join.getJoinType().equals(JoinType.RIGHT)) {
                    throw new JoinerException("Fetch is not supported for right join!");
                }
                joinerVendorRepository.addFetch(query, join);
            }
        }
    }

    private void resolveJoinAlias(Set<Path<?>> usedAliases, JoinDescription join) {
        if (join.getAlias() == null) {
            setAliasFromResolver(join);

            if (join.getAlias() == null) {
                setDefaultAlias(join);
            }
        }

        if (usedAliases.contains(join.getAlias())) {
            throw new AliasAlreadyUsedException("Alias " + join.getAlias() + " is already used!");
        }
    }

    private void checkAliasesArePresent(Q<T> request, Set<Path<?>> usedAliases) {
        if (request.getWhere() instanceof Operation) {
            for (Object o : ((Operation) request.getWhere()).getArgs()) {
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
            join.alias(resolveAlias(join.getCollectionPath()));
        } else {
            join.alias(resolveAlias(join.getSinglePath()));
        }
    }

    private EntityPath<?> resolveAlias(Path<?> path) {
        if (aliasResolvers != null) {
            for (JoinerAliasResolver aliasResolver : aliasResolvers) {
                EntityPath<?> resolved = aliasResolver.resolveAlias(path);
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
                        "Set full join path. For example 'QUser.user.address' instead of 'QAddress.address' ");
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
