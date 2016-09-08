package cz.encircled.joiner.query.join;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtil;

/**
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

    @SuppressWarnings("unchcecked")
    public static <T extends EntityPath> T path(EntityPath<?> grandFather, EntityPath<?> father, T path) {
        Assert.notNull(father);
        Assert.notNull(grandFather);

        EntityPath<?> parentPath = JoinerUtil.getAliasForChild(grandFather, father);

        return JoinerUtil.getAliasForChild(parentPath, path);
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
