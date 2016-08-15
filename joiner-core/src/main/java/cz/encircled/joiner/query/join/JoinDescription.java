package cz.encircled.joiner.query.join;

import com.mysema.query.JoinType;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.CollectionPathBase;
import cz.encircled.joiner.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents query join.
 * For collection joins - {@link JoinDescription#collectionPath collectionPath} is used, for single entity joins - {@link JoinDescription#singlePath singlePath}.
 * <p>
 *     By default, all joins are <b>left fetch</b> joins
 * </p>
 *
 * @author Kisel on 21.01.2016.
 */
public class JoinDescription {

    private CollectionPathBase<?, ?, ?> collectionPath;

    private EntityPath<?> singlePath;

    private EntityPath<?> alias;

    private JoinType joinType = JoinType.LEFTJOIN;

    private boolean fetch = true;

    private Predicate on;

    private JoinDescription parent;

    private List<JoinDescription> children = new ArrayList<>();

    JoinDescription(EntityPath<?> alias) {
        Assert.notNull(alias);

        this.alias = alias;
    }

    public boolean isFetch() {
        return fetch;
    }

    public JoinDescription fetch(final boolean fetch) {
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

    public JoinDescription alias(EntityPath<?> alias) {
        this.alias = alias;
        return this;
    }

    public CollectionPathBase<?, ?, ?> getCollectionPath() {
        return collectionPath;
    }

    public EntityPath<?> getSinglePath() {
        return singlePath;
    }

    public JoinDescription singlePath(EntityPath<?> path) {
        Assert.notNull(path);

        singlePath = path;
        collectionPath = null;
        return this;
    }

    public JoinDescription collectionPath(CollectionPathBase<?, ?, ?> path) {
        Assert.notNull(path);

        collectionPath = path;
        singlePath = null;
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

    public JoinDescription nested(JoinDescription... joins) {
        for (JoinDescription join : joins) {
            join.parent = this;
            children.add(join);
        }
        return this;
    }

    public JoinDescription getParent() {
        return parent;
    }

    public List<JoinDescription> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JoinDescription)) return false;

        JoinDescription that = (JoinDescription) o;

        if (!alias.equals(that.alias)) return false;
        return parent != null ? parent.equals(that.parent) : that.parent == null;

    }

    @Override
    public int hashCode() {
        int result = alias.hashCode();
        result = 31 * result + (parent != null ? parent.hashCode() : 0);
        return result;
    }
}
