package cz.encircled.joiner.query.join;

import com.querydsl.core.JoinType;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CollectionPathBase;
import cz.encircled.joiner.query.JoinRoot;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents query join.
 * For collection joins - {@link JoinDescription#collectionPath collectionPath} is used, for single entity joins - {@link JoinDescription#singularPath singlePath}.
 * <p>
 * By default, all joins are <b>left fetch</b> joins
 * </p>
 *
 * @author Kisel on 21.01.2016.
 */
public class JoinDescription implements JoinRoot {

    private final EntityPath<?> originalAlias;
    private CollectionPathBase<?, ?, ?> collectionPath;
    private EntityPath<?> singularPath;
    private EntityPath<?> alias;
    private JoinType joinType = JoinType.LEFTJOIN;
    private final Expression<?> path;

    private boolean fetch = true;

    private Predicate on;

    private JoinDescription parent;

    private Map<String, JoinDescription> children = new LinkedHashMap<>(4);

    public JoinDescription(EntityPath<?> alias) {
        this(alias, alias);
    }

    public JoinDescription(Expression<?> path, EntityPath<?> alias) {
        Assert.notNull(alias);
        this.path = path;

        if (alias.getMetadata().getParent() != null) {
            // TODO if parent is present, then it should be moved to a nested join perhaps? Unless parent is the root
            this.originalAlias = JoinerUtils.getLastElementPath(alias);
            this.alias = originalAlias;
        } else {
            this.originalAlias = alias;
            this.alias = alias;
        }
    }

    public JoinDescription copy() {
        JoinDescription copy = new JoinDescription(path, originalAlias);
        copy.alias = alias;

        copy.children = new HashMap<>(children.size());
        children.forEach((k, v) -> copy.children.put(k, v.copy()));

        copy.fetch = fetch;
        copy.on = on;
        copy.parent = parent;
        copy.collectionPath = collectionPath;
        copy.singularPath = singularPath;
        copy.joinType = joinType;
        return copy;
    }

    public boolean isFetch() {
        return fetch;
    }

    public JoinDescription fetch(boolean fetch) {
        this.fetch = fetch;
        return this;
    }

    public JoinDescription on(Predicate on) {
        this.on = on;
        return this;
    }

    public Predicate getOn() {
        return on;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    private JoinDescription joinType(final JoinType joinType) {
        this.joinType = joinType;
        return this;
    }

    public EntityPath<?> getAlias() {
        return alias;
    }

    public EntityPath<?> getOriginalAlias() {
        return originalAlias;
    }

    /**
     * Set different alias for the join
     */
    public JoinDescription alias(EntityPath<?> alias) {
        this.alias = alias;
        return this;
    }

    public CollectionPathBase<?, ?, ?> getCollectionPath() {
        return collectionPath;
    }

    public EntityPath<?> getSingularPath() {
        return singularPath;
    }

    public JoinDescription singularPath(EntityPath<?> path) {
        Assert.notNull(path);

        singularPath = path;
        collectionPath = null;
        return this;
    }

    public JoinDescription collectionPath(CollectionPathBase<?, ?, ?> path) {
        Assert.notNull(path);

        collectionPath = path;
        singularPath = null;
        return this;
    }

    public boolean isCollectionPath() {
        return collectionPath != null;
    }

    public JoinDescription inner() {
        return joinType(JoinType.INNERJOIN);
    }

    public JoinDescription left() {
        return joinType(JoinType.LEFTJOIN);
    }

    public JoinDescription right() {
        return joinType(JoinType.RIGHTJOIN);
    }

    /**
     * Add children joins to current join
     *
     * @param joins children joins
     * @return current join
     */
    public JoinDescription nested(JoinDescription... joins) {
        for (JoinDescription join : joins) {
            join.parent = this;
            join.alias(J.path(this.getAlias(), join.getOriginalAlias()));
            addJoin(join);

            reAliasChildren(join);
        }

        return this;
    }

    /**
     * Re-alias due to order of 'nested' method execution (the very last nested is executed first, and it's parent is not re-aliased yet)
     */
    private void reAliasChildren(JoinDescription join) {
        for (JoinDescription child : join.children.values()) {
            child.alias(J.path(join.getAlias(), child.getOriginalAlias()));
            reAliasChildren(child);
        }
    }

    /**
     * Add children joins to current join from specified paths
     *
     * @param paths children join paths
     * @return current join
     */
    public JoinDescription nested(EntityPath<?>... paths) {
        for (EntityPath<?> path : paths) {
            nested(J.left(path));
        }

        return this;
    }

    /**
     * Add children joins to current join from specified paths
     *
     * @param paths children join paths
     * @return current join
     */
    public JoinDescription nested(CollectionPathBase<?, ?, ?>... paths) {
        for (CollectionPathBase<?, ?, ?> path : paths) {
            nested(J.left(path));
        }

        return this;
    }

    public JoinDescription getParent() {
        return parent;
    }

    public Collection<JoinDescription> getChildren() {
        return children.values();
    }

    @Override
    public Map<String, JoinDescription> getAllJoins() {
        return children;
    }

    /**
     * @return initial join path. Might be singular or collection
     */
    public Expression<?> getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinDescription that)) return false;

        if (!alias.equals(that.alias)) return false;
        return parent != null ? parent.equals(that.parent) : that.parent == null;

    }

    @Override
    public int hashCode() {
        int result = alias.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "JoinDescription{" +
                "collectionPath=" + collectionPath +
                ", singlePath=" + singularPath +
                ", alias=" + alias +
                '}';
    }

}
