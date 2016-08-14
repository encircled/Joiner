package cz.encircled.joiner.repository;

import com.google.common.collect.ArrayListMultimap;
import com.mysema.query.JoinType;
import com.mysema.query.jpa.impl.AbstractJPAQuery;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPathBase;
import com.mysema.query.types.path.EntityPathBase;
import cz.encircled.joiner.exception.AliasMissingException;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.query.QueryFeature;
import cz.encircled.joiner.repository.vendor.EclipselinkRepository;
import cz.encircled.joiner.repository.vendor.HibernateRepository;
import cz.encircled.joiner.repository.vendor.JoinerVendorRepository;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtil;
import cz.encircled.joiner.util.ReflectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static cz.encircled.joiner.util.ReflectionUtils.getField;

/**
 * @author Kisel on 26.01.2016.
 */
public class Joiner {

    private static final Path<?> nullPath = new BooleanPath("");

    private final Map<Pair<Class, Class>, Path> aliasCache = new ConcurrentHashMap<>();

    private EntityManager entityManager;

    private JoinerVendorRepository joinerVendorRepository;

    public Joiner(EntityManager entityManager) {
        Assert.notNull(entityManager);

        this.entityManager = entityManager;

        String implName = entityManager.getDelegate().getClass().getName();
        if (implName.startsWith("org.hibernate")) {
            this.joinerVendorRepository = new HibernateRepository();
        } else if (implName.startsWith("org.eclipse")) {
            this.joinerVendorRepository = new EclipselinkRepository();
        }
    }

    public <T> T findOne(Q<T> request) {
        return findOne(request, request.getFrom());
    }

    public <T, P> P findOne(Q<T> request, Expression<P> projection) {
        List<P> list = find(request, projection);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() == 1) {
            return list.get(0);
        } else {
            throw new JoinerException("FindOne returned multiple records!");
        }
    }

    public <T> List<T> find(Q<T> request) {
        Assert.notNull(request);
        return find(request, request.getFrom());
    }

    public <T, P> List<P> find(Q<T> request, Expression<P> projection) {
        Assert.notNull(request);
        Assert.notNull(projection);
        // TODO extract validation
        Assert.notNull(request.getFrom());

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

        for (JoinDescription join : request.getJoins()) {
            resolveJoinAlias(usedAliases, join, request.getFrom());
        }

        addJoins(request, query, request.getFrom().equals(projection));
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

        return query.list(projection);
    }

    private JPAQuery doPostProcess(Q<?> request, JPAQuery query, QueryFeature feature) {
        return feature.after(request, query);
    }

    private <T> Q<T> doPreProcess(Q<T> request, QueryFeature feature) {
        return feature.before(request);
    }

    private void makeInsertionOrderHints(AbstractJPAQuery<JPAQuery> sourceQuery) {
        Field f = ReflectionUtils.findField(AbstractJPAQuery.class, "hints");
        ReflectionUtils.setField(f, sourceQuery, ArrayListMultimap.create());
    }

    private void addJoins(Q<?> request, JPAQuery query, boolean canFetch) {
        List<JoinDescription> joins = new ArrayList<>();
        for (JoinDescription join : request.getJoins()) {
            collectChildren(join, joins);
        }

        for (JoinDescription join : joins) {
            joinerVendorRepository.addJoin(query, join);
            if (canFetch && join.isFetch()) {
                if (join.getJoinType().equals(JoinType.RIGHTJOIN)) {
                    throw new JoinerException("Fetch is not supported for right join!");
                }
                joinerVendorRepository.addFetch(query, join, request.getJoins(), request.getFrom());
            }
        }
    }

    private void collectChildren(JoinDescription join, List<JoinDescription> collection) {
        collection.add(join);
        for (JoinDescription child : join.getChildren()) {
            collectChildren(child, collection);
        }
    }

    private void addHints(Q<?> request, JPAQuery query) {
        for (Map.Entry<String, List<Object>> entry : request.getHints().entrySet()) {
            if (entry.getValue() != null) {
                for (Object value : entry.getValue()) {
                    query.setHint(entry.getKey(), value);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void resolveJoinAlias(Set<Path<?>> usedAliases, JoinDescription join, EntityPath<?> root) {
        Path<?> parent = join.getParent() != null ? join.getParent().getAlias() : root;
        Class<?> targetType = join.getAlias().getType();

        Path<?> fieldOnParent = findPathOnParent(parent, targetType, join);
        if (fieldOnParent instanceof CollectionPathBase) {
            join.collectionPath((CollectionPathBase<?, ?, ?>) fieldOnParent);
        } else if (fieldOnParent instanceof EntityPath) {
            join.singlePath((EntityPath<?>) fieldOnParent);
        }
        if (join.getParent() != null) {
            join.alias(JoinerUtil.getAliasForChild(join.getParent().getAlias(), join.getAlias()));
        }

        usedAliases.add(join.getAlias());

        for (JoinDescription child : join.getChildren()) {
            resolveJoinAlias(usedAliases, child, root);
        }

    }

    private Path<?> findPathOnParent(Object parent, Class<?> targetType, JoinDescription joinDescription) {
        while (!targetType.equals(Object.class)) {
            Pair<Class, Class> cacheKey = Pair.of(parent.getClass(), targetType);
            Path cached = aliasCache.get(cacheKey);
            if (cached != null && !cached.equals(nullPath)) {
                // TODO test
                // TODO optimize inheritance cases
                return cached;
            }

            Path<?> result = null;

            for (Field field : parent.getClass().getFields()) {
                Object candidate = getField(field, parent);

                if (candidate instanceof CollectionPathBase) {
                    Field elementTypeField = ReflectionUtils.findField(candidate.getClass(), "elementType");
                    Class<?> elementType = (Class<?>) getField(elementTypeField, candidate);

                    if (elementType.equals(targetType)) {
                        result = (Path<?>) candidate;
                    }
                } else if (candidate instanceof EntityPathBase) {
                    Class type = ((EntityPathBase) candidate).getType();
                    if (type.equals(targetType)) {
                        result = (Path<?>) candidate;
                    }
                }
            }

            if (result == null) {
                joinDescription.fetch(false);
                for (JoinDescription child : joinDescription.getChildren()) {
                    child.fetch(false);
                }
                aliasCache.put(cacheKey, nullPath);
                targetType = targetType.getSuperclass();
            } else {
                aliasCache.put(cacheKey, result);
                return result;
            }
        }

        return null;
    }

    private void checkAliasesArePresent(Expression<?> expression, Set<Path<?>> usedAliases) {
        for (Path<?> path : resolvePaths(expression)) {
            Path predicatePath = path.getRoot();
            if (!predicatePath.toString().startsWith("any(")) {
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

}
