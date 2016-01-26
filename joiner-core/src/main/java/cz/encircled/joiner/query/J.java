package cz.encircled.joiner.query;

import javax.persistence.criteria.JoinType;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.path.CollectionPathBase;

/**
 * @author Kisel on 26.01.2016.
 */
public class J {

    public static JoinDescription join(CollectionPathBase<?, ?, ?> path) {
        return new JoinDescription(path);
    }

    public static JoinDescription join(EntityPath<?> path) {
        return new JoinDescription(path);
    }

    public static JoinDescription join(CollectionPathBase<?, ?, ?> path, JoinType type) {
        return new JoinDescription(path).joinType(type);
    }

    public static JoinDescription join(EntityPath<?> path, JoinType type) {
        return new JoinDescription(path).joinType(type);
    }

}
