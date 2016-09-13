package cz.encircled.joiner.core;

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
import cz.encircled.joiner.core.vendor.HibernateRepository;
import cz.encircled.joiner.core.vendor.JoinerVendorRepository;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtil;
import cz.encircled.joiner.util.ReflectionUtils;

/**
 * @author Kisel on 26.01.2016.
 */
public class Joiner {

    private EntityManager entityManager;

    private JoinerVendorRepository joinerVendorRepository;

    private JoinGraphRegistry joinGraphRegistry;

    private AliasResolver aliasResolver;

    public Joiner(EntityManager entityManager) {
        Assert.notNull(entityManager);

        this.entityManager = entityManager;
        aliasResolver = new DefaultAliasResolver(entityManager);

        String implName = entityManager.getDelegate().getClass().getName();
        if (implName.startsWith("org.hibernate")) {
            this.joinerVendorRepository = new HibernateRepository();
        } else if (implName.startsWith("org.eclipse")) {
            try {
                Class<?> eclipseLink = Class.forName("cz.encircled.joiner.eclipse.EclipselinkRepository");
                this.joinerVendorRepository = (JoinerVendorRepository) eclipseLink.newInstance();
            } catch (Exception e) {
                throw new JoinerException("EclipseLink support module is missing!", e);
            }
        }
    }

    public <T, R> R findOne(JoinerQuery<T, R> request) {
        List<R> list = find(request);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new JoinerException("FindOne returned multiple records!");
        }
    }

    public <T, R> List<R> find(JoinerQuery<T, R> request) {
        Assert.notNull(request);
        // TODO extract validation
        Assert.notNull(request.getFrom());

        setJoinsFromJoinsGraphs(request);

        for (QueryFeature feature : request.getFeatures()) {
            request = doPreProcess(request, feature);
        }

        JPAQuery query = joinerVendorRepository.createQuery(entityManager);
        makeInsertionOrderHints(query);

        query.from(request.getFrom());
        if (request.isDistinct()) {
            query.distinct();
        }

        Set<Path<?>> usedAliases = new HashSet<>();
        usedAliases.add(request.getFrom());

        List<JoinDescription> joins = JoinerUtil.unrollChildrenJoins(request.getJoins());
        for (JoinDescription join : joins) {
            if (join.getCollectionPath() == null && join.getSinglePath() == null) {
                aliasResolver.resolveJoinAlias(join, request.getFrom());
            }
            usedAliases.add(join.getAlias());
        }
        addJoins(joins, query, request.getFrom(), request.getFrom().equals(request.getReturnProjection(query)));

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

        for (QueryFeature feature : request.getFeatures()) {
            query = doPostProcess(request, query, feature);
        }

        return joinerVendorRepository.getResultList(query, request.getReturnProjection(query));
    }

    private <T, R> void setJoinsFromJoinsGraphs(JoinerQuery<T, R> request) {
        // TODO copy joins
        if (!request.getJoinGraphs().isEmpty()) {
            if (joinGraphRegistry == null) {
                throw new JoinerException("Join graph are set, but joinGraphRegistry is null!");
            }

            Class<? extends T> queryRootClass = request.getFrom().getType();

            for (String name : request.getJoinGraphs()) {
                List<JoinDescription> joins = joinGraphRegistry.getJoinGraph(queryRootClass, name);
                if (joins == null) {
                    throw new JoinerException(String.format("JoinGraph with name [%s] is not defined for class [%s]", name, queryRootClass));
                } else {
                    request.joins(joins);
                }
            }
        }
    }

    private JPAQuery doPostProcess(JoinerQuery<?, ?> request, JPAQuery query, QueryFeature feature) {
        return feature.after(request, query);
    }

    private <T, R> JoinerQuery<T, R> doPreProcess(JoinerQuery<T, R> request, QueryFeature feature) {
        return feature.before(request);
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<JPAQuery> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.setField(f, sourceQuery, ArrayListMultimap.create());
    }

    private void addJoins(List<JoinDescription> joins, JPAQuery query, EntityPath<?> rootPath, boolean doFetch) {
        for (JoinDescription join : joins) {
            joinerVendorRepository.addJoin(query, join);
            if (doFetch && join.isFetch()) {
                if (join.getJoinType().equals(JoinType.RIGHTJOIN)) {
                    throw new JoinerException("Fetch is not supported for right join!");
                }
                joinerVendorRepository.addFetch(query, join, joins, rootPath);
            }
        }
    }

    private void addHints(JoinerQuery<?, ?> request, JPAQuery query) {
        for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
            if (entry.getValue() != null) {
                for (Object value : entry.getValue()) {
                    query.setHint(entry.getKey(), value);
                }
            }
        }
    }

    private void checkAliasesArePresent(Expression<?> expression, Set<Path<?>> usedAliases) {
        for (Path<?> path : collectPredicatePaths(expression)) {
            Path predicatePath = path.getRoot();
            if (!predicatePath.toString().startsWith("any(")) {
                if (!usedAliases.contains(predicatePath)) {
                    throw new AliasMissingException("Alias " + predicatePath + " is not present in joins!");
                }
            }
        }
    }

    private List<Path<?>> collectPredicatePaths(Expression<?> expression) {
        List<Path<?>> result = new ArrayList<>();
        collectPredicatePathsInternal(expression, result);
        return result;
    }

    private void collectPredicatePathsInternal(Expression<?> expression, List<Path<?>> paths) {
        if (expression instanceof Path) {
            paths.add((Path<?>) expression);
        } else if (expression instanceof Operation) {
            for (Expression exp : ((Operation<?>) expression).getArgs()) {
                collectPredicatePathsInternal(exp, paths);
            }
        }
    }

    public void setJoinGraphRegistry(JoinGraphRegistry joinGraphRegistry) {
        this.joinGraphRegistry = joinGraphRegistry;
    }

}
