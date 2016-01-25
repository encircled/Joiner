package cz.encircled.joiner.query;

import javax.persistence.criteria.JoinType;

import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;

/**
 * @author Kisel on 21.01.2016.
 */
public class JoinDescription {

    private ListPath<?, ?> listPath;

    private Path<?> alias;

    private JoinType joinType = JoinType.LEFT;

    private boolean fetch = true;

    public JoinDescription(ListPath<?, ?> listPath) {
        this.listPath = listPath;
    }

    public boolean isFetch() {
        return fetch;
    }

    public JoinDescription fetch(final boolean fetch) {
        this.fetch = fetch;
        return this;
    }

    public JoinType getJoinType() {
        return joinType;
    }

    public JoinDescription joinType(final JoinType joinType) {
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

    public ListPath<?, ?> getListPath() {
        return listPath;
    }

    public JoinDescription listPath(ListPath<?, ?> listPath) {
        this.listPath = listPath;
        return this;
    }

}
