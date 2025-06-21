package cz.encircled.joiner.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CollectionPathBase;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtils;
import jakarta.persistence.FlushModeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of joiner query with {@link com.querydsl.core.Tuple non-tuple} result
 *
 * @author Kisel on 13.9.2016.
 */
public class JoinerQueryBase<T, R> implements JoinerQuery<T, R> {

    private final EntityPath<T> from;
    private Expression<R> returnProjection;

    private Predicate where;

    /**
     * Alias to join
     */
    private final Map<String, JoinDescription> joins = new LinkedHashMap<>(8);

    private final Set<Object> joinGraphs = new LinkedHashSet<>();

    private boolean distinct = true;

    private Path<?>[] groupBy;

    private Predicate having;

    private LinkedHashMap<String, List<Object>> hints = new LinkedHashMap<>(2);

    private List<QueryFeature> features = new ArrayList<>(2);

    private Integer offset;

    private Integer limit;

    private List<QueryOrder> orders = new ArrayList<>(2);

    private FlushModeType flushMode;

    private Boolean cacheable;
    private String cacheRegion;

    private Integer timeout;

    private boolean isCount;

    private Boolean isStatelessSession;

    protected JoinDescription lastJoin;

    public JoinerQueryBase(EntityPath<T> from) {
        this.from = from;
    }

    JoinerQueryBase(EntityPath<T> from, boolean isCount) {
        this.from = from;
        this.isCount = isCount;
    }

    public JoinerQueryBase(EntityPath<T> from, Expression<R> returnProjection) {
        this.from = from;
        this.returnProjection = returnProjection;
    }

    @Override
    public Predicate getWhere() {
        return where;
    }

    @Override
    public JoinerQueryBase<T, R> distinct(boolean isDistinct) {
        distinct = isDistinct;
        return this;
    }

    @Override
    public JoinerQueryBase<T, R> groupBy(Path<?>... groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    @Override
    public Path<?>[] getGroupBy() {
        return groupBy;
    }

    @Override
    public boolean isDistinct() {
        return distinct;
    }

    @Override
    public JoinerQueryBase<T, R> where(Predicate where) {
        this.where = where;
        return this;
    }

    @Override
    public JoinerQueryBase<T, R> andWhere(BooleanExpression where) {
        if (this.where != null) {
            where(where.and(this.where));
            return this;
        } else {
            return where(where);
        }
    }

    @Override
    public JoinerQuery<T, R> orWhere(BooleanExpression where) {
        if (this.where != null) {
            where(where.or(this.where));
            return this;
        } else {
            return where(where);
        }
    }

    @Override
    public JoinerQueryBase<T, R> having(Predicate having) {
        this.having = having;
        return this;
    }

    @Override
    public Predicate getHaving() {
        return having;
    }

    @Override
    public EntityPath<T> getFrom() {
        return from;
    }

    @Override
    public Set<Object> getJoinGraphs() {
        return joinGraphs;
    }

    @Override
    public Map<String, JoinDescription> getAllJoins() {
        return joins;
    }

    @Override
    public Collection<JoinDescription> getJoins() {
        return joins.values();
    }

    @Override
    public JoinDescription removeJoin(JoinDescription join) {
        return joins.remove(join.getOriginalAlias().toString());
    }

    @Override
    public JoinerQueryBase<T, R> joinGraphs(final String... names) {
        Collections.addAll(joinGraphs, names);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joinGraphs(final Enum... names) {
        Collections.addAll(joinGraphs, names);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joinGraphs(Collection<?> names) {
        Assert.notNull(names);

        joinGraphs.addAll(names);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(EntityPath<?>... paths) {
        for (EntityPath<?> path : paths) {
            Assert.notNull(path);

            addJoin(J.left(path));
        }

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(CollectionPathBase<?, ?, ?>... paths) {
        for (CollectionPathBase<?, ?, ?> path : paths) {
            joins(((EntityPath<?>) JoinerUtils.getDefaultPath(path)));
        }

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(JoinDescription... joins) {
        for (JoinDescription join : joins) {
            Assert.notNull(join);

            addJoin(join);
        }

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(Collection<JoinDescription> joins) {
        Assert.notNull(joins);

        joins.forEach(this::addJoin);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> addHint(String hint, Object value) {
        Assert.notNull(hint);

        hints.computeIfAbsent(hint, h -> new ArrayList<>(2));
        hints.get(hint).add(value);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> addFeatures(QueryFeature... features) {
        Assert.notNull(features);

        Collections.addAll(this.features, features);
        return this;
    }

    @Override
    public JoinerQueryBase<T, R> addFeatures(Collection<QueryFeature> features) {
        Assert.notNull(features);

        this.features.addAll(features);
        return this;
    }

    @Override
    public List<QueryFeature> getFeatures() {
        return features;
    }

    @Override
    public LinkedHashMap<String, List<Object>> getHints() {
        return hints;
    }

    @Override
    public Expression<R> getReturnProjection() {
        return returnProjection;
    }

    @Override
    public JoinerQueryBase<T, R> offset(Integer offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Integer getOffset() {
        return offset;
    }

    @Override
    public JoinerQueryBase<T, R> limit(Integer limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Integer getLimit() {
        return limit;
    }

    @Override
    public JoinerQueryBase<T, R> asc(Expression<?> orderBy) {
        Assert.notNull(orderBy);

        orders.add(new QueryOrder<>(true, orderBy));
        return this;
    }

    @Override
    public JoinerQueryBase<T, R> desc(Expression<?> orderBy) {
        Assert.notNull(orderBy);

        orders.add(new QueryOrder<>(false, orderBy));
        return this;
    }

    @Override
    public List<QueryOrder> getOrder() {
        return orders;
    }

    @Override
    public JoinerQuery<T, R> copy() {
        JoinerQueryBase<T, R> copy = Q.select(returnProjection)
                .from(from)
                .joinGraphs(new LinkedHashSet<>(joinGraphs))
                .joins(getJoins().stream().map(JoinDescription::copy).collect(Collectors.toList()))
                .addFeatures(new ArrayList<>(getFeatures()))
                .where(where)
                .offset(offset)
                .limit(limit)
                .groupBy(groupBy)
                .having(this.having);

        copy.isCount = isCount;
        copy.orders = new ArrayList<>(orders);

        // TODO deep-deep copy?
        copy.hints = new LinkedHashMap<>(hints);

        return copy;
    }

    @Override
    public JoinerQuery<T, Tuple> copy(Expression<?>[] newReturnProjections) {
        JoinerQueryBase<T, Tuple> copy = Q.select(newReturnProjections)
                .from(from)
                .joinGraphs(new LinkedHashSet<>(joinGraphs))
                .joins(getJoins().stream().map(JoinDescription::copy).collect(Collectors.toList()))
                .addFeatures(new ArrayList<>(getFeatures()))
                .where(where)
                .offset(offset)
                .limit(limit)
                .groupBy(groupBy)
                .having(this.having);

        copy.isCount = isCount;
        copy.orders = new ArrayList<>(orders);

        copy.hints = new LinkedHashMap<>(hints);

        return copy;
    }

    @Override
    public boolean isCount() {
        return isCount;
    }

    public void count() {
        isCount = true;
    }

    @Override
    public FlushModeType getFlushMode() {
        return flushMode;
    }

    @Override
    public JoinerQuery<T, R> flushMode(FlushModeType flushMode) {
        this.flushMode = flushMode;
        return this;
    }

    @Override
    public Boolean getCacheable() {
        return cacheable;
    }

    @Override
    public JoinerQuery<T, R> cacheable(Boolean cacheable) {
        this.cacheable = cacheable;
        return this;
    }

    @Override
    public String getCacheRegion() {
        return cacheRegion;
    }

    @Override
    public JoinerQuery<T, R> cacheRegion(String cacheRegion) {
        this.cacheRegion = cacheRegion;
        return this;
    }

    @Override
    public Integer getTimeout() {
        return timeout;
    }

    @Override
    public JoinerQuery<T, R> timeout(Integer timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public Boolean isStatelessSession() {
        return isStatelessSession;
    }

    @Override
    public JoinerQuery<T, R> setStatelessSession(Boolean statelessSession) {
        isStatelessSession = statelessSession;
        return this;
    }

    @Override
    public JoinerQuery<T, R> useStatelessSession() {
        return setStatelessSession(true);
    }

    @Override
    public String toString() {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        return serializer.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinerQueryBase)) return false;
        JoinerQueryBase<?, ?> that = (JoinerQueryBase<?, ?>) o;
        return distinct == that.distinct &&
                isCount == that.isCount &&
                Objects.equals(from, that.from) &&
                Objects.equals(returnProjection, that.returnProjection) &&
                Objects.equals(where, that.where) &&
                Objects.equals(joins, that.joins) &&
                Objects.equals(joinGraphs, that.joinGraphs) &&
                Objects.equals(groupBy, that.groupBy) &&
                Objects.equals(having, that.having) &&
                Objects.equals(hints, that.hints) &&
                Objects.equals(features, that.features) &&
                Objects.equals(offset, that.offset) &&
                Objects.equals(limit, that.limit) &&
                Objects.equals(orders, that.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, returnProjection, where, joins, joinGraphs, distinct, groupBy, having, hints, features, offset, limit, orders, isCount);
    }

    @Override
    public <R1, C> @Nullable R1 accept(Visitor<R1, C> v, @Nullable C context) {
        return null;
    }

    @Override
    public Class<? extends R> getType() {
        return null;
    }
}

