package cz.encircled.joiner.query;

import java.util.ArrayList;
import java.util.List;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Predicate;

/**
 * @author Kisel on 11.01.2016.
 */
public class Q<T> {

    private Predicate predicate;

    private EntityPath<T> rootEntityPath;

    private List<JoinDescription> joins = new ArrayList<JoinDescription>();

    private boolean distinct = true;

    public static <T> Q<T> from(EntityPath<T> from) {
        return new Q<T>().rootEntityPath(from);
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Q<T> distinct(boolean isDistinct) {
        distinct = isDistinct;
        return this;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public Q<T> where(Predicate predicate) {
        this.predicate = predicate;
        return this;
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

    public Q<T> addJoin(JoinDescription join) {
        if (joins == null) {
            joins = new ArrayList<JoinDescription>();
        }

        joins.add(join);
        return this;
    }

    public Q<T> joins(List<JoinDescription> joins) {
        this.joins = joins;
        return this;
    }

}
