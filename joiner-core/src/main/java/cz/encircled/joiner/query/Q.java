package cz.encircled.joiner.query;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.Assert;

import java.util.*;

/**
 * This class is a transfer object for repository queries.
 * <p>
 * <p>
 * T - root entity type
 * </p>
 *
 * @author Kisel on 11.01.2016.
 * @see JoinDescription
 */
public class Q<T> {

    private final EntityPath<T> from;
    private Predicate where;
    private Set<JoinDescription> joins = new LinkedHashSet<>();

    private List<String> joinGraphs = new ArrayList<>();

    private boolean distinct = true;

    private Expression<?> groupBy;

    private Predicate having;

    private LinkedHashMap<String, List<Object>> hints = new LinkedHashMap<>(2);

    private List<QueryFeature> features = new ArrayList<>(2);

    public Q(EntityPath<T> from) {
        this.from = from;
    }

    public static <T> Q<T> from(EntityPath<T> from) {
        return new Q<>(from);
    }

    public Predicate getWhere() {
        return where;
    }

    public Q<T> distinct(boolean isDistinct) {
        distinct = isDistinct;
        return this;
    }

    public Q<T> groupBy(Expression<?> groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public Expression<?> getGroupBy() {
        return groupBy;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public Q<T> where(Predicate where) {
        this.where = where;
        return this;
    }

    public Q<T> having(Predicate having) {
        this.having = having;
        return this;
    }

    public Predicate getHaving() {
        return having;
    }

    public EntityPath<T> getFrom() {
        return from;
    }

    public List<String> getJoinGraphs() {
        return joinGraphs;
    }

    public Set<JoinDescription> getJoins() {
        return joins;
    }

    public Q<T> joinGraphs(String... names) {
        Collections.addAll(joinGraphs, names);

        return this;
    }

    public Q<T> joins(JoinDescription... joins) {
        return joins(Arrays.asList(joins));
    }

    public Q<T> joins(Collection<JoinDescription> joins) {
        Assert.notNull(joins);

        for (JoinDescription join : joins) {
            if (!this.joins.add(join)) {
                if (join.getChildren() != null) {
                    joins(join.getChildren());
                }
            }
        }

        return this;
    }

    public Q<T> addHint(String hint, Object value) {
        Assert.notNull(hint);

        hints.computeIfAbsent(hint, h -> new ArrayList<>(2));
        hints.get(hint).add(value);

        return this;
    }

    public Q<T> addFeatures(QueryFeature... features) {
        Collections.addAll(this.features, features);
        return this;
    }

    public List<QueryFeature> getFeatures() {
        return features;
    }

    public LinkedHashMap<String, List<Object>> getHints() {
        return hints;
    }

}
