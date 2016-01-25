package cz.encircled.joiner;

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

    public static <T> Q<T> from(EntityPath<T> from) {
        return new Q<T>().rootEntityPath(from);
    }

    public Predicate getPredicate() {
        return predicate;
    }

    public Q<T> predicate(Predicate predicate) {
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

    public Q<T> joins(List<JoinDescription> joins) {
        this.joins = joins;
        return this;
    }

}
