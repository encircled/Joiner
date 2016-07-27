package cz.encircled.joiner.query;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * This class is a transfer object for repository queries.
 *
 * <p>
 *  T - root entity type
 * </p>
 *
 * @see JoinDescription
 * @author Kisel on 11.01.2016.
 */
public class Q<T> {

    private Predicate where;

    private EntityPath<T> rootEntityPath;

    private List<JoinDescription> joins = new ArrayList<>();

    private boolean distinct = true;

    private Expression<?> groupBy;

    private Predicate having;

    private LinkedHashMap<String, List<Object>> hints = new LinkedHashMap<>(2);

    private List<QueryFeature> features = new ArrayList<>(2);

    public static <T> Q<T> from(EntityPath<T> from) {
        return new Q<T>().rootEntityPath(from);
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

    public EntityPath<T> getRootEntityPath() {
        return rootEntityPath;
    }

    public Q<T> rootEntityPath(EntityPath<T> rootEntityPath) {
        this.rootEntityPath = rootEntityPath;
        return this;
    }

    public List<JoinDescription> getJoins() {
        return joins;
    }

    public Q<T> setJoins(List<JoinDescription> joins) {
        this.joins = joins;
        return this;
    }

    public Q<T> join(JoinDescription join) {
        if (joins == null) {
            joins = new ArrayList<>();
        }

        joins.add(join);
        return this;
    }

    public Q<T> join(Path<?> path) {
        if (joins == null) {
            joins = new ArrayList<>();
        }

        joins.add(J.join(path));
        return this;
    }

    public Q<T> joins(Path<?>... paths) {
        for (Path<?> path : paths) {
            join(path);
        }
        return this;
    }

    public Q<T> joins(List<JoinDescription> joins) {
        Assert.notNull(joins);

        if (this.joins == null) {
            this.joins = new ArrayList<>();
        }

        this.joins.addAll(joins);
        return this;
    }

    public Q<T> addHint(String hint, Object value) {
        Assert.notNull(hint);

        List<Object> values = hints.get(hint);

        if (values == null) {
            values = new ArrayList<>(2);
            hints.put(hint, values);
        }

        values.add(value);
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
