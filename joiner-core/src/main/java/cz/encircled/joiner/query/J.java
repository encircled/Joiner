package cz.encircled.joiner.query;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.util.JoinerUtil;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO add join labels for grouping
 * <p>
 * Util class, which helps to build new {@link JoinDescription joins}
 *
 * @author Kisel on 26.01.2016.
 */
public class J {

    @SuppressWarnings("unchcecked")
    public static <T extends EntityPath> T path(EntityPath<?> parent, T path) {
        if (parent != null) {
            return JoinerUtil.getAliasForChild(parent, path);
        }
        return path;
    }

    public static List<JoinDescription> joins(EntityPath<?>... paths) {
        List<JoinDescription> result = new ArrayList<JoinDescription>(paths.length);
        for (EntityPath<?> path : paths) {
            result.add(left(path));
        }
        return result;
    }

    public static JoinDescription left(EntityPath<?> path) {
        return getBasicJoin(path).left();
    }

    public static JoinDescription inner(EntityPath<?> path) {
        return getBasicJoin(path).inner();
    }

    private static JoinDescription getBasicJoin(EntityPath<?> path) {
        Assert.notNull(path);

        return new JoinDescription(path);
    }

}
