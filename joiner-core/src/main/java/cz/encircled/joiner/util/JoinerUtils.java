package cz.encircled.joiner.util;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.CollectionPathBase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vlad on 29-Dec-16.
 */
public final class JoinerUtils {

    private JoinerUtils() {

    }

    public static List<Path<?>> collectPredicatePaths(Expression<?> expression) {
        List<Path<?>> result = new ArrayList<>();
        collectPredicatePathsInternal(expression, result);
        return result;
    }

    // TODO cache? and test
    /**
     * Find default path of an entity (e.g. QUser.user for QUser class)
     */
    public static <T extends EntityPath<?>> T getDefaultPath(Class<T> entityPath) {
        // Default path name is equal to the class name without `Q` prefix
        String name = StringUtils.uncapitalize(entityPath.getSimpleName().substring(1));

        Field f = ReflectionUtils.findField(entityPath, name);
        if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
            // TODO iterate deeper...
            f = ReflectionUtils.findField(entityPath, name + "1");
        }
        return (T) ReflectionUtils.getField(f, null);
    }

    /**
     * Find default path of an entity (e.g. QUser.user for QUser class)
     */
    public static <T extends EntityPath<?>> T getDefaultPath(CollectionPathBase<?, ?, ?> path) {
        Class qClass = (Class<?>) ReflectionUtils.getField(ReflectionUtils.findField(path.getClass(), "queryType"), path);
        return (T) getDefaultPath(qClass);
    }

    private static void collectPredicatePathsInternal(Expression<?> expression, List<Path<?>> paths) {
        if (expression instanceof Path) {
            paths.add((Path<?>) expression);
        } else if (expression instanceof Operation) {
            for (Expression exp : ((Operation<?>) expression).getArgs()) {
                collectPredicatePathsInternal(exp, paths);
            }
        }
    }

}
