package cz.encircled.joiner.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.JoinType;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.CollectionPathBase;
import cz.encircled.joiner.exception.JoinerException;
import org.springframework.util.Assert;

/**
 * @author Kisel on 26.01.2016.
 */
public class J {

    public static List<JoinDescription> joins(Path<?>... paths) {
        List<JoinDescription> result = new ArrayList<JoinDescription>(paths.length);
        for (Path<?> path : paths) {
            result.add(join(path));
        }
        return result;
    }

    public static JoinDescription join(Path<?> path) {
        return join(path, JoinType.LEFT);
    }

    public static JoinDescription join(Path<?> path, JoinType joinType) {
        Assert.notNull(path);
        Assert.notNull(joinType);

        if (path instanceof EntityPath) {
            return new JoinDescription((EntityPath<?>) path).joinType(joinType);
        } else if (path instanceof CollectionPathBase<?, ?, ?>) {
            return new JoinDescription((CollectionPathBase<?, ?, ?>) path).joinType(joinType);
        } else {
            throw new JoinerException("Unsupported path type: " + path);
        }
    }

}
