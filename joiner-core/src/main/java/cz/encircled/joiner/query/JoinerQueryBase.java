package cz.encircled.joiner.query;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.Assert;

import java.util.*;

/**
 * @author Kisel on 13.9.2016.
 */
public class JoinerQueryBase<T, R> implements JoinerQuery<T, R> {

    private final EntityPath<T> from;
    private Expression<R> returnProjection;

    private Predicate where;
    /**
     * Alias to join
     */
    private Map<String, JoinDescription> joins = new LinkedHashMap<>();

    private List<Object> joinGraphs = new ArrayList<>();

    private boolean distinct = true;

    private Expression<?> groupBy;

    private Predicate having;

    private LinkedHashMap<String, List<Object>> hints = new LinkedHashMap<>(2);

    private List<QueryFeature> features = new ArrayList<>(2);

    private Long offset;

    private Long limit;

    private List<QueryOrder> orders = new ArrayList<>(2);

    public JoinerQueryBase(EntityPath<T> from, Expression<R> returnProjection) {
        this.from = from;
        this.returnProjection = returnProjection;
    }

    protected JoinerQueryBase(EntityPath<T> from) {
        this.from = from;
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
    public JoinerQueryBase<T, R> groupBy(Expression<?> groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    @Override
    public Expression<?> getGroupBy() {
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
    public List<Object> getJoinGraphs() {
        return joinGraphs;
    }

    @Override
    public Collection<JoinDescription> getJoins() {
        return joins.values();
    }

    @Override
    public JoinDescription getJoin(Expression<?> expression) {
        Assert.notNull(expression);

        return joins.get(expression.toString());
    }

    @Override
    public JoinerQueryBase<T, R> joinGraphs(Object... names) {
        Collections.addAll(joinGraphs, names);

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(EntityPath<?>... paths) {
        for (EntityPath<?> path : paths) {
            joins(J.left(path));
        }

        return this;
    }

    @Override
    public JoinerQueryBase<T, R> joins(JoinDescription... joins) {
        return joins(Arrays.asList(joins));
    }

    @Override
    public JoinerQueryBase<T, R> joins(Collection<JoinDescription> joins) {
        Assert.notNull(joins);

        for (JoinDescription join : joins) {
            this.joins.put(join.getOriginalAlias().toString(), join);
        }

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
    public Expression<R> getReturnProjection(JPAQuery query) {
        return returnProjection;
    }

    @Override
    public JoinerQueryBase<T, R> offset(Long offset) {
        this.offset = offset;
        return this;
    }

    @Override
    public Long getOffset() {
        return offset;
    }

    @Override
    public JoinerQueryBase<T, R> limit(Long limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Long getLimit() {
        return limit;
    }

    @Override
    public JoinerQueryBase<T, R> asc(Expression<?> orderBy) {
        Assert.notNull(orderBy);

        orders.add(new QueryOrder(true, orderBy));
        return this;
    }

    @Override
    public JoinerQueryBase<T, R> desc(Expression<?> orderBy) {
        Assert.notNull(orderBy);

        orders.add(new QueryOrder(false, orderBy));
        return this;
    }

    @Override
    public List<QueryOrder> getOrder() {
        return orders;
    }

}

