package cz.encircled.joiner.core;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPathBase;
import com.mysema.query.types.path.EntityPathBase;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.JoinerUtil;
import cz.encircled.joiner.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cz.encircled.joiner.util.ReflectionUtils.getField;

/**
 * @author Vlad on 16-Aug-16.
 */
public class DefaultAliasResolver implements AliasResolver {

    private static final Path<?> nullPath = new BooleanPath("");

    private final Map<String, Path> aliasCache = new ConcurrentHashMap<>();

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
            String cacheKey = parent.getClass().getName() + targetType.getSimpleName() + joinDescription.getAlias().toString();
            Path cached = aliasCache.get(cacheKey);
            if (cached != null && !cached.equals(nullPath)) {
                // TODO test
                // TODO optimize inheritance cases
                return cached;
            }

            List<Path<?>> candidatePaths = new ArrayList<>();

            for (Field field : parent.getClass().getFields()) {
                Object candidate = getField(field, parent);

                if (candidate instanceof CollectionPathBase) {
                    Field elementTypeField = ReflectionUtils.findField(candidate.getClass(), "elementType");
                    Class<?> elementType = (Class<?>) getField(elementTypeField, candidate);

                    if (elementType.equals(targetType)) {
                        candidatePaths.add((Path<?>) candidate);
                    }
                } else if (candidate instanceof EntityPathBase) {
                    Class type = ((EntityPathBase) candidate).getType();
                    if (type.equals(targetType)) {
                        candidatePaths.add((Path<?>) candidate);
                    }
                }
            }

            if (candidatePaths.isEmpty()) {
                // TODO may be exception?
                joinDescription.fetch(false);
                for (JoinDescription child : joinDescription.getChildren()) {
                    child.fetch(false);
                }
                aliasCache.put(cacheKey, nullPath);
                targetType = targetType.getSuperclass();
            } else if (candidatePaths.size() == 1) {
                aliasCache.put(cacheKey, candidatePaths.get(0));
                return candidatePaths.get(0);
            } else {
                // Multiple associations on parent, try find by specified alias
                String targetFieldName = joinDescription.getAlias().toString();
                for (Path<?> candidatePath : candidatePaths) {
                    if (targetFieldName.equals(candidatePath.getMetadata().getElement())) {
                        aliasCache.put(cacheKey, candidatePath);
                        return candidatePath;
                    }
                }
                throw new JoinerException("Join with ambiguous alias : " + joinDescription + ". Multiple mappings found");
            }
        }

        return null;
    }

}
