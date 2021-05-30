package cz.encircled.joiner.query.join;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.SimpleExpression;
import cz.encircled.joiner.util.Assert;
import cz.encircled.joiner.util.JoinerUtils;
import cz.encircled.joiner.util.ReflectionUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains helper methods for {@link JoinDescription joins} building
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
     * To refer a <code>Contact</code> entity in the 'where' clause, one should use <code>J.path(QPerson.person.contacts).number.eq(12345)</code>
     *
     * @param path path on parent entity
     * @param <T>  any entity path
     * @return entity path with correct alias
     */
    @SuppressWarnings("unchcecked")
    public static <P extends SimpleExpression<?>, T extends EntityPath<P>> P path(CollectionPathBase<?, ?, P> path) {
        Assert.notNull(path);
        EntityPath<?> current = JoinerUtils.getDefaultPath(path);
        return ReflectionUtils.instantiate(current.getClass(), current + "_on_" + path.getMetadata().getParent());
    }

    /**
     * Aliases of nested joins are determined at runtime. To refer a nested join, this method should be used to get a correct alias.
     * For example, there is a query
     * <p>
     * <code>Q.from(QGroup.group).joins(J.left(QPerson.person).nested(J.left(QContact.contact)))</code>
     * </p>
     * To refer a <code>Contact</code> entity in the 'where' clause, one should use <code>J.path(QPerson.person, QContact.contact).number.eq(12345)</code>
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

    /**
     * Aliases of nested joins are determined at runtime. To refer a nested join, this method should be used to get a correct alias.
     * For example, there is a query
     * <p>
     * <code>Q.from(QGroup.group).joins(J.left(QPerson.person).nested(J.left(QContact.contact).nested(QStatus.status)))</code>
     * </p>
     * To refer a <code>Status</code> entity in the 'where' clause, one should use <code>J.path(QPerson.person, QContact.contact. QStatus.status).state.eq("active")</code>
     *
     * @param grandFather parent of parent join path
     * @param father      parent join path
     * @param path        target join path
     * @param <T>         any entity path
     * @return entity path with correct alias
     */
    @SuppressWarnings("unchcecked")
    public static <T extends EntityPath> T path(EntityPath<?> grandFather, EntityPath<?> father, T path) {
        Assert.notNull(father);
        Assert.notNull(grandFather);

        EntityPath<?> parentPath = path(grandFather, father);

        return path(parentPath, path);
    }

    /**
     * Add <b>left</b> join for given <code>path</code>
     *
     * @param path alias of object to be joined
     * @return join description
     */
    public static JoinDescription left(EntityPath<?> path) {
        return getBasicJoin(path).left();
    }

    /**
     * Add <b>right</b> join for given <code>path</code>
     *
     * @param path alias of object to be joined
     * @return join description
     */
    public static JoinDescription right(EntityPath<?> path) {
        return getBasicJoin(path).right();
    }

    /**
     * Add <b>left</b> join for given <code>path</code>
     *
     * @param path path to an object to be joined
     * @return join description
     */
    public static JoinDescription left(CollectionPathBase<?, ?, ?> path) {
        return getBasicJoin(JoinerUtils.getDefaultPath(path)).left();
    }

    /**
     * Add <b>inner</b> join for given <code>path</code>
     *
     * @param path alias of object to be joined
     * @return join description
     */
    public static JoinDescription inner(EntityPath<?> path) {
        return getBasicJoin(path).inner();
    }

    /**
     * Add <b>inner</b> join for given <code>path</code>
     *
     * @param path path to an object to be joined
     * @return join description
     */
    public static JoinDescription inner(CollectionPathBase<?, ?, ?> path) {
        return getBasicJoin(JoinerUtils.getDefaultPath(path)).inner();
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
