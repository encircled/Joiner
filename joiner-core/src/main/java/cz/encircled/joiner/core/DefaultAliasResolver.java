package cz.encircled.joiner.core;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPathBase;
import com.mysema.query.types.path.EntityPathBase;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.JoinerUtil;
import cz.encircled.joiner.util.ReflectionUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cz.encircled.joiner.util.ReflectionUtils.getField;

/**
 * @author Vlad on 16-Aug-16.
 */
public class DefaultAliasResolver implements AliasResolver {

    private static final Path<?> nullPath = new BooleanPath("");

    private final Map<Pair<Class, Class>, Path> aliasCache = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void resolveJoinAlias(JoinDescription join, EntityPath<?> root) {
        Path<?> parent = join.getParent() != null ? join.getParent().getAlias() : root;
        Class<?> targetType = join.getAlias().getType();

        Path<?> fieldOnParent = findPathOnParent(parent, targetType, join);
        if (fieldOnParent instanceof CollectionPathBase) {
            join.collectionPath((CollectionPathBase<?, ?, ?>) fieldOnParent);
        } else if (fieldOnParent instanceof EntityPath) {
            join.singlePath((EntityPath<?>) fieldOnParent);
        }
        if (join.getParent() != null) {
            join.alias(JoinerUtil.getAliasForChild(join.getParent().getAlias(), join.getAlias()));
        }
    }

    private Path<?> findPathOnParent(Object parent, Class<?> targetType, JoinDescription joinDescription) {
        while (!targetType.equals(Object.class)) {
            Pair<Class, Class> cacheKey = Pair.of(parent.getClass(), targetType);
            Path cached = aliasCache.get(cacheKey);
            if (cached != null && !cached.equals(nullPath)) {
                // TODO test
                // TODO optimize inheritance cases
                return cached;
            }

            Path<?> result = null;

            for (Field field : parent.getClass().getFields()) {
                Object candidate = getField(field, parent);

                if (candidate instanceof CollectionPathBase) {
                    Field elementTypeField = ReflectionUtils.findField(candidate.getClass(), "elementType");
                    Class<?> elementType = (Class<?>) getField(elementTypeField, candidate);

                    if (elementType.equals(targetType)) {
                        result = (Path<?>) candidate;
                    }
                } else if (candidate instanceof EntityPathBase) {
                    Class type = ((EntityPathBase) candidate).getType();
                    if (type.equals(targetType)) {
                        result = (Path<?>) candidate;
                    }
                }
            }

            if (result == null) {
                joinDescription.fetch(false);
                for (JoinDescription child : joinDescription.getChildren()) {
                    child.fetch(false);
                }
                aliasCache.put(cacheKey, nullPath);
                targetType = targetType.getSuperclass();
            } else {
                aliasCache.put(cacheKey, result);
                return result;
            }
        }

        return null;
    }

}
