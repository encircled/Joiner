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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vlad on 29-Dec-16.
 */
public final class JoinerUtils {

    static Map<Class<?>, EntityPath<?>> qClassToDefaultEntity = new ConcurrentHashMap<>();

    private JoinerUtils() {

    }

    public static List<Path<?>> collectPredicatePaths(Expression<?> expression) {
        List<Path<?>> result = new ArrayList<>();
        collectPredicatePathsInternal(expression, result);
        return result;
    }

    /**
     * Find default path of an entity (e.g. QUser.user for QUser class)
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntityPath<?>> T getDefaultPath(Class<T> qClass) {
        return (T) qClassToDefaultEntity.computeIfAbsent(qClass, c -> {
            // Default path name is equal to the class name without `Q` prefix
            String name = StringUtils.uncapitalize(qClass.getSimpleName().substring(1));

            Field f = ReflectionUtils.findField(qClass, name);
            for (int i = 1; i < 3; i++) {
                if (!Modifier.isStatic(f.getModifiers()) || !Modifier.isFinal(f.getModifiers())) {
                    f = ReflectionUtils.findField(qClass, name + i);
                }
            }

            return (T) ReflectionUtils.getField(f, null);
        });
    }

    /**
     * Find default path of an entity (e.g. QUser.user for QUser class)
     */
    public static <T extends EntityPath<?>> T getDefaultPath(CollectionPathBase<?, ?, ?> path) {
        Class qClass = (Class<?>) ReflectionUtils.getField(ReflectionUtils.findField(path.getClass(), "queryType"), path);
        return (T) getDefaultPath(qClass);
    }

    /**
     *
     * @param entityPath entity path, possibly with parent. i.e. QUser.user.phone
     * @return path for the last child element, i.e. QPhone for the QUser.user.phone
     */
    public static EntityPath<?> getLastElementPath(EntityPath<?> entityPath) {
        if (entityPath.getMetadata().getParent() == null) {
            return entityPath;
        }
        try {
            Object targetName = entityPath.getMetadata().getElement();
            return entityPath.getClass().getConstructor(String.class).newInstance(targetName.toString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
