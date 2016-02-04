package cz.encircled.joiner.repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import com.google.common.collect.ArrayListMultimap;
import com.mysema.query.JoinType;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.CollectionPathBase;
import cz.encircled.joiner.alias.JoinerAliasResolver;
import cz.encircled.joiner.exception.AliasAlreadyUsedException;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.InsufficientSinglePathException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.vendor.EclipselinkRepository;
import cz.encircled.joiner.repository.vendor.HibernateRepository;
import cz.encircled.joiner.repository.vendor.JoinerVendorRepository;
import cz.encircled.joiner.util.JoinerUtil;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

/**
 * @author Kisel on 26.01.2016.
 */
public class Joiner<T> implements QRepository<T> {

    private EntityManager entityManager;

    private EntityPath<T> rootPath;

    private JoinerVendorRepository joinerVendorRepository;

    private List<JoinerAliasResolver> aliasResolvers;

    private List<QueryPostProcessor> postProcessors;

    public Joiner(EntityManager entityManager, EntityPath<T> rootPath) {
        Assert.notNull(entityManager);
        Assert.notNull(rootPath);

        this.entityManager = entityManager;
        this.rootPath = rootPath;

        String implName = entityManager.getDelegate().getClass().getName();
        if (implName.startsWith("org.hibernate")) {
            this.joinerVendorRepository = new HibernateRepository();
        } else if (implName.startsWith("org.eclipse")) {
            this.joinerVendorRepository = new EclipselinkRepository();
        }
    }

    public void setPostProcessors(List<QueryPostProcessor> postProcessors) {
        this.postProcessors = postProcessors;
    }

    public void setAliasResolvers(List<JoinerAliasResolver> aliasResolvers) {
        this.aliasResolvers = aliasResolvers;
    }

    @Override
    public T findOne(Q<T> request) {
        return null;
    }

    @Override
    public <P> P findOne(Q<T> request, Expression<P> projection) {
        return null;
    }

    @Override
    public List<T> find(Q<T> request) {
        return find(request, rootPath);
    }

    @Override
    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        Assert.notNull(request);
        Assert.notNull(projection);

        JPAQuery query = joinerVendorRepository.createQuery(entityManager);
        makeInsertionOrderHints(query);

        if (request.getRootEntityPath() == null) {
            request.rootEntityPath(rootPath);
        }
        query.from(request.getRootEntityPath());
        if (request.isDistinct()) {
            query.distinct();
        }

        Set<Path<?>> usedAliases = new HashSet<>();
        usedAliases.add(request.getRootEntityPath());

        for (JoinDescription join : request.getJoins()) {
            checkSinglePathCompletion(join);
            resolveJoinAlias(usedAliases, join);
        }

        addJoins(request, query);
        addHints(request, query);

        checkAliasesArePresent(request.getWhere(), usedAliases);
        checkAliasesArePresent(request.getHaving(), usedAliases);
        checkAliasesArePresent(request.getGroupBy(), usedAliases);

        query.where(request.getWhere());
        if (request.getGroupBy() != null) {
            query.groupBy(request.getGroupBy());
        }
        if (request.getHaving() != null) {
            query.having(request.getHaving());
        }

        doPostProcess(query);

        return query.list(projection);
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<JPAQuery> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.makeAccessible(f);
        ReflectionUtils.setField(f, sourceQuery, ArrayListMultimap.create());
    }

    private void addJoins(Q<T> request, JPAQuery query) {
        for (JoinDescription join : request.getJoins()) {
            joinerVendorRepository.addJoin(query, join);
            if (join.isFetch()) {
                if (join.getJoinType().equals(JoinType.RIGHTJOIN)) {
                    throw new JoinerException("Fetch is not supported for right join!");
                }
                joinerVendorRepository.addFetch(query, join, request.getJoins(), request.getRootEntityPath());
            }
        }
    }

    private void addHints(Q<T> request, JPAQuery query) {
        for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
            if (entry.getValue() != null) {
                for (Object value : entry.getValue()) {
                    query.setHint(entry.getKey(), value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveJoinAlias(Set<Path<?>> usedAliases, JoinDescription join) {
        if (join.getAlias() == null) {
            if (join.isCollectionPath()) {
                join.alias(resolveAlias(join.getCollectionPath()));
            } else {
                join.alias(resolveAlias(join.getSinglePath()));
            }
        }

        if (usedAliases.contains(join.getAlias())) {
            throw new AliasAlreadyUsedException("Alias " + join.getAlias() + " is already used!");
        }

        Path<?> root = join.isCollectionPath() ? join.getCollectionPath().getRoot() : join.getSinglePath().getRoot();
        if (!usedAliases.contains(root)) {
            EntityPath<?> generated = JoinerUtil.getGenerated(root, null);

            String alias = resolveAlias((JoinerUtil.getSuper(generated))).toString();
            EntityPath<?> parent = JoinerUtil.instantiate((Class<EntityPath<?>>) generated.getClass(), alias);

            Object targetField = JoinerUtil.findAndGetField(parent, ((Field) join.getAnnotatedElement()).getName());

            if (join.isCollectionPath()) {
                join.collectionPath((CollectionPathBase<?, ?, ?>) targetField);
            } else {
                join.singlePath((EntityPath<?>) targetField);
            }

            if (usedAliases.contains(parent.getRoot())) {
                if (join.isCollectionPath()) {
                    join.alias(resolveAlias((CollectionPathBase<?, ?, ?>) targetField));
                } else {
                    join.alias(resolveAlias((EntityPath<?>) targetField));
                }
            } else {
                throw new AliasMissingException("Can't join " + join + ", alias " + join.getAlias() + " is not present!");
            }
        }

        usedAliases.add(join.getAlias());
    }

    private void checkAliasesArePresent(Expression<?> expression, Set<Path<?>> usedAliases) {
        for (Path<?> path : resolvePaths(expression)) {
            Path predicatePath = path.getRoot();
            if (predicatePath.toString().startsWith("any(")) {
                // TODO what to do?
            } else {
                if (!usedAliases.contains(predicatePath)) {
                    throw new AliasMissingException("Alias " + predicatePath + " is not present in joins!");
                }
            }
        }
    }

    private List<Path<?>> resolvePaths(Expression<?> expression) {
        List<Path<?>> result = new ArrayList<>();
        resolvePathsInternal(expression, result);
        return result;
    }

    private void resolvePathsInternal(Expression<?> expression, List<Path<?>> paths) {
        if (expression instanceof Path) {
            paths.add((Path<?>) expression);
        } else if (expression instanceof Operation) {
            for (Expression exp : ((Operation<?>) expression).getArgs()) {
                resolvePathsInternal(exp, paths);
            }
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
        if (path instanceof CollectionPathBase) {
            return JoinerUtil.getDefaultAlias((CollectionPathBase) path);
        } else {
            return JoinerUtil.getDefaultAlias((EntityPath) path);
        }
    }

    private void checkSinglePathCompletion(JoinDescription join) {
        if (!join.isCollectionPath()) {
            if (join.getSinglePath().toString().equals(join.getSinglePath().getRoot().toString())) {
                throw new InsufficientSinglePathException(
                        "Set full join path. For example 'QUser.user.address' instead of 'QAddress.address' ");
            }
        }
    }

    private void doPostProcess(JPAQuery query) {
        if (postProcessors != null) {
            for (QueryPostProcessor postProcessor : postProcessors) {
                postProcessor.process(query);
            }
        }
    }

}
