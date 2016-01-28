package cz.encircled.joiner.query;

import com.mysema.query.JoinType;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.CollectionPathBase;
import org.springframework.util.Assert;

/**
 * @author Kisel on 21.01.2016.
 */
public class JoinDescription {

    private CollectionPathBase<?, ?, ?> collectionPath;

    private EntityPath<?> singlePath;

    private Path<?> alias;

    private JoinType joinType = JoinType.LEFTJOIN;

    private boolean fetch = true;

    private Predicate on;

    public JoinDescription(CollectionPathBase<?, ?, ?> collectionPath) {
        Assert.notNull(collectionPath);

        this.collectionPath = collectionPath;
    }

    public JoinDescription(EntityPath<?> singlePath) {
        Assert.notNull(singlePath);

        this.singlePath = singlePath;
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

    public Path<?> getAlias() {
        return alias;
    }

    public JoinDescription alias(final Path<?> alias) {
        this.alias = alias;
        return this;
    }

    public CollectionPathBase<?, ?, ?> getCollectionPath() {
        return collectionPath;
    }

    public EntityPath<?> getSinglePath() {
        return singlePath;
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

}
