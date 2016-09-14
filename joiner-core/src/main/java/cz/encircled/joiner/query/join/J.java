package cz.encircled.joiner.query.join;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.ReflectionUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Util class, which helps to build new {@link JoinDescription joins}
 *
 * @author Kisel on 26.01.2016.
 */
public class J {

    /**
     * Aliases of nested joins are determined at runtime. To refer a nested join, this method should be used to get a correct alias.
     * For example, there is a query
     * <p>
     * <code>Q.from(QGroup.group).joins(J.left(QPerson.person).nested(J.left(QContact.contact)))</code>
     * </p>
     * To refer a contact entity for instance in the 'where' clause, one should use <code>J.path(QPerson.person, QContact.contact).number.eq(12345)</code>
     *
     * @param parent parent join path
     * @param path   target join path
     * @param <T>    any entity path
     * @return entity path with correct alias
     */
    @SuppressWarnings("unchcecked")
    public static <T extends EntityPath> T path(EntityPath<?> parent, T path) {
        if (parent != null) {
            return ReflectionUtils.instantiate(path.getClass(), path.toString() + "_on_" + parent.toString());
        }
        return path;
    }

    @SuppressWarnings("unchcecked")
    public static <T extends EntityPath> T path(EntityPath<?> grandFather, EntityPath<?> father, T path) {
        Assert.notNull(father);
        Assert.notNull(grandFather);

        EntityPath<?> parentPath = path(grandFather, father);

        return path(parentPath, path);
    }

    public static JoinDescription left(EntityPath<?> path) {
        return getBasicJoin(path).left();
    }

    public static JoinDescription inner(EntityPath<?> path) {
        return getBasicJoin(path).inner();
    }

    /**
     * Collect all joins and its children to single collection
     *
     * @param joins root joins
     * @return all joins, including children
     */
    public static List<JoinDescription> unrollChildrenJoins(Collection<JoinDescription> joins) {
        List<JoinDescription> collection = new LinkedList<>();

        for (JoinDescription joinDescription : joins) {
            unrollChildrenInternal(joinDescription, collection);
        }

        return collection;
    }

    private static void unrollChildrenInternal(JoinDescription join, List<JoinDescription> collection) {
        collection.add(join);
        for (JoinDescription child : join.getChildren()) {
            unrollChildrenInternal(child, collection);
        }
    }

    private static JoinDescription getBasicJoin(EntityPath<?> path) {
        Assert.notNull(path);

        return new JoinDescription(path);
    }

}
