package cz.encircled.joiner.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Kisel on 25.01.2016.
 */
public class JoinerUtil {

    public static List<JoinDescription> unrollChildrenJoins(Set<JoinDescription> joins) {
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

    public static <T extends EntityPath> T getAliasForChild(EntityPath<?> parent, T childPath) {
        return ReflectionUtils.instantiate(childPath.getClass(), childPath.toString() + "_on_" + parent.toString());
    }

}
