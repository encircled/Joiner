package cz.encircled.joiner.core;

import com.querydsl.core.types.*;
import cz.encircled.joiner.core.vendor.EclipselinkRepository;
import cz.encircled.joiner.core.vendor.HibernateRepository;
import cz.encircled.joiner.core.vendor.JoinerVendorRepository;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.exception.JoinerExceptions;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import cz.encircled.joiner.util.JoinerUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.stream.Collectors;

import static cz.encircled.joiner.util.Assert.notNull;

/**
 * Base class of Joiner. Contains basic database operations.
 * <p>
 * In spring-based environment can be instantiated using spring JoinerConfiguration.
 * </p>
 * <p>
 * For repository-per-entity approach this class should not be accessed directly. Instead repositories can implement {@link JoinerRepository}.
 * </p>
 *
 * @author Kisel on 26.01.2016.
 */
public class Joiner {
    // TODO query level conf ?

    private static final Logger log = LoggerFactory.getLogger(Joiner.class);

    private final EntityManager entityManager;

    private final JoinerVendorRepository joinerVendorRepository;

    private JoinGraphRegistry joinGraphRegistry;

    private final AliasResolver aliasResolver;

    private final PredicateAliasResolver predicateAliasResolver = new DefaultPredicateAliasResolver(this);

    private JoinerProperties joinerProperties;

    public Joiner(EntityManager entityManager) {
        this(entityManager, new JoinerProperties());
    }

    public Joiner(EntityManager entityManager, JoinerProperties joinerProperties) {
        notNull(entityManager);

        this.joinerProperties = joinerProperties;
        this.entityManager = entityManager;
        this.aliasResolver = new DefaultAliasResolver(entityManager);

        String implName = entityManager.getDelegate().getClass().getName();
        this.joinerVendorRepository = implName.startsWith("org.hibernate") ? new HibernateRepository() : new EclipselinkRepository();
    }

    public Joiner withProperties(JoinerProperties props) {
        return new Joiner(entityManager, props);
    }

    public <T, R> R findOne(JoinerQuery<T, R> request) {
        List<R> list = find(request);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw JoinerExceptions.multipleEntitiesFound();
        }
    }

    public <T, R> List<R> find(JoinerQuery<T, R> request) {
        toJPAQuery(request);

        List<R> result = joinerVendorRepository.getResultList(request, getJoinerProperties(), entityManager);

        for (QueryFeature queryFeature : getQueryFeatures(request)) {
            queryFeature.postLoad(request, result);
        }

        return result;
    }

    public <I, T extends Collection<I>> T save(T entities) {
        for (Object entity : entities) {
            save(entity);
        }
        return entities;
    }

    public <T> T save(T entity) {
        entityManager.persist(entity);
        return entity;
    }

    public <T, R> void toJPAQuery(JoinerQuery<T, R> request) {
        notNull(request);
        notNull(request.getFrom());

        setJoinsFromJoinsGraphs(request);

        List<QueryFeature> queryFeatures = getQueryFeatures(request);
        for (QueryFeature feature : queryFeatures) {
            request = doPreProcess(request, feature);
        }

        Set<Path<?>> usedAliases = new HashSet<>();
        usedAliases.add(request.getFrom());

        List<JoinDescription> joins = preprocessJoins(request, usedAliases);

        addJoins(joins, request);

        applyPredicates(request, usedAliases, joins);

        applyPaging(request, usedAliases, joins);

        for (QueryFeature feature : queryFeatures) {
            doPostProcess(request, null, feature);
        }
    }

    private <T, R> List<QueryFeature> getQueryFeatures(JoinerQuery<T, R> request) {
        if (!joinerProperties.defaultFeatures.isEmpty()) {
            ArrayList<QueryFeature> res = new ArrayList<>(joinerProperties.defaultFeatures);
            res.addAll(request.getFeatures());
            return res;
        }
        return request.getFeatures();
    }

    private <T, R> List<JoinDescription> preprocessJoins(JoinerQuery<T, R> request, Set<Path<?>> usedAliases) {
        List<JoinDescription> joins = J.unrollChildrenJoins(request.getJoins());
        for (JoinDescription join : joins) {
            if (join.getCollectionPath() == null && join.getSingularPath() == null) {
                aliasResolver.resolveFieldPathForJoinAlias(join, request.getFrom());
            }
            usedAliases.add(join.getAlias());
        }
        for (JoinDescription join : joins) {
            if (join.getOn() != null) {
                join.on(predicateAliasResolver.resolvePredicate(join.getOn(), joins, usedAliases));
            }
        }
        return joins;
    }

    /**
     * Apply "where", "groupBy" and "having"
     *
     * @param request
     * @param usedAliases
     * @param joins
     * @param <T>
     * @param <R>
     */
    private <T, R> void applyPredicates(JoinerQuery<T, R> request, Set<Path<?>> usedAliases, List<JoinDescription> joins) {
        if (request.getWhere() != null) {
            Predicate where = predicateAliasResolver.resolvePredicate(request.getWhere(), joins, usedAliases);
            checkAliasesArePresent(where, usedAliases);
            request.where(where);
        }
        if (request.getGroupBy() != null) {
            Map<AnnotatedElement, List<JoinDescription>> grouped = joins.stream()
                    .collect(Collectors.groupingBy(j -> j.getOriginalAlias().getAnnotatedElement()));
            for (Path<?> path : request.getGroupBy()) {
                Path<?> grouping = predicateAliasResolver.resolvePath(path, grouped, usedAliases);
                checkAliasesArePresent(grouping, usedAliases);
                request.groupBy(grouping);
            }
        }
        if (request.getHaving() != null) {
            Predicate having = predicateAliasResolver.resolvePredicate(request.getHaving(), joins, usedAliases);
            checkAliasesArePresent(having, usedAliases);
            request.having(having);
        }
    }

    private <T, R> void applyPaging(JoinerQuery<T, R> request, Set<Path<?>> usedAliases, List<JoinDescription> joins) {
        Map<AnnotatedElement, List<JoinDescription>> grouped = joins.stream()
                .collect(Collectors.groupingBy(j -> j.getOriginalAlias().getAnnotatedElement()));
        for (QueryOrder queryOrder : request.getOrder()) {
            if (queryOrder.getTarget() instanceof Path<?>) {
                Path<?> path = predicateAliasResolver.resolvePath((Path<?>) queryOrder.getTarget(), grouped, usedAliases);
                queryOrder = new QueryOrder(queryOrder.isAsc(), path);
            }

            checkAliasesArePresent(queryOrder.getTarget(), usedAliases);
//            request.orderBy(transformOrder(queryOrder)); TODO
        }
    }

    private <T extends Comparable<?>> OrderSpecifier<T> transformOrder(QueryOrder<T> queryOrder) {
        return new OrderSpecifier<>(queryOrder.isAsc() ? Order.ASC : Order.DESC, queryOrder.getTarget());
    }

    private <T, R> void setJoinsFromJoinsGraphs(JoinerQuery<T, R> request) {
        if (!request.getJoinGraphs().isEmpty()) {
            if (joinGraphRegistry == null) {
                throw new JoinerException("Join graph are set, but joinGraphRegistry is null!");
            }

            Class<? extends T> queryRootClass = request.getFrom().getType();

            for (Object name : request.getJoinGraphs()) {
                List<JoinDescription> joins = joinGraphRegistry.getJoinGraph(queryRootClass, name);

                // Fetch is not allowed in count queries
                if (request.isCount()) {
                    J.unrollChildrenJoins(joins).forEach(j -> j.fetch(false));
                }

                request.joins(joins);
            }
        }
    }

    private <T, R> Query doPostProcess(JoinerQuery<T, R> request, Query query, QueryFeature feature) {
        return feature.after(request, query);
    }

    private <T, R> JoinerQuery<T, R> doPreProcess(JoinerQuery<T, R> request, QueryFeature feature) {
        return feature.before(request);
    }

    private void addJoins(List<JoinDescription> joins, JoinerQuery<?, ?> request) {
        EntityPath<?> rootPath = request.getFrom();
        boolean doFetch = request.getFrom().equals(request.getReturnProjection());
        for (JoinDescription join : joins) {
            if (doFetch && join.isFetch()) {
                joinerVendorRepository.addFetch(join, joins, rootPath, request);
            }
        }
    }

    private void checkAliasesArePresent(Expression<?> expression, Set<Path<?>> usedAliases) {
        for (Path<?> path : JoinerUtils.collectPredicatePaths(expression)) {
            Path<?> predicatePath = path.getRoot();
            if (!predicatePath.toString().startsWith("any(")) {
                if (!usedAliases.contains(predicatePath)) {
                    throw new AliasMissingException("Alias " + predicatePath + " is not present in joins!");
                }
            }
        }
    }

    public void setJoinGraphRegistry(JoinGraphRegistry joinGraphRegistry) {
        this.joinGraphRegistry = joinGraphRegistry;
    }

    public JoinerProperties getJoinerProperties() {
        return joinerProperties;
    }

    public void setJoinerProperties(JoinerProperties joinerProperties) {
        this.joinerProperties = Objects.requireNonNullElse(joinerProperties, new JoinerProperties());
    }

}
