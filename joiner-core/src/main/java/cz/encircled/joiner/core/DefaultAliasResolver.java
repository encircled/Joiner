package cz.encircled.joiner.core;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanPath;
import com.querydsl.core.types.dsl.CollectionPathBase;
import com.querydsl.core.types.dsl.EntityPathBase;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.ReflectionUtils;
import jakarta.persistence.EntityManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cz.encircled.joiner.util.JoinerUtils.getQClass;
import static cz.encircled.joiner.util.ReflectionUtils.getField;

/**
 * @author Vlad on 16-Aug-16.
 * @see AliasResolver
 */
public class DefaultAliasResolver implements AliasResolver {

    private static final BooleanPath nullPath = ReflectionUtils.instantiate(BooleanPath.class, "");

    private final EntityManager entityManager;

    private final Map<String, Path<?>> aliasCache = new ConcurrentHashMap<>();

    public DefaultAliasResolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void resolveFieldPathForJoinAlias(JoinDescription join, EntityPath<?> root) {
        Path<?> parent = join.getParent() != null ? join.getParent().getAlias() : root;
        Path<?> fieldOnParent = findPathOnParent(parent, join.getAlias().getType(), join);

        if (fieldOnParent instanceof CollectionPathBase) {
            join.collectionPath((CollectionPathBase<?, ?, ?>) fieldOnParent);
        } else if (fieldOnParent instanceof EntityPath) {
            join.singularPath((EntityPath<?>) fieldOnParent);
        } else {
            throw new JoinerException("Target field not found for join " + join);
        }
    }

    private Path<?> findPathOnParent(Path<?> parent, Class<?> targetType, JoinDescription joinDescription) {
        String cacheKey = joinDescription.getOriginalAlias().toString() + parent.toString() + parent.getClass().getSimpleName() + targetType.getSimpleName();
        if (aliasCache.containsKey(cacheKey)) {
            return aliasCache.get(cacheKey);
        }

        Path<?> result = doFindPathOnParent(parent, targetType, joinDescription);
        aliasCache.put(cacheKey, result);
        return result;
    }

    /**
     * @param parent          parent join or "from" element
     * @param targetType      type of joined attribute
     * @param joinDescription join description
     * @return path on parent Q class
     */
    protected Path<?> doFindPathOnParent(Path<?> parent, Class<?> targetType, JoinDescription joinDescription) {
        List<Path<?>> candidatePaths = new ArrayList<>();

        for (Field field : parent.getClass().getDeclaredFields()) {
            // skip self-reference and 'super' reference
            if (!Modifier.isStatic(field.getModifiers()) && !field.getName().equals("_super")) {
                testAliasCandidate(targetType, candidatePaths, getField(field, parent));
            }
        }

        if (candidatePaths.isEmpty()) {
            findPathOnParentSubclasses(parent, targetType, candidatePaths);
        }

        switch (candidatePaths.size()) {
            case 0:
                J.unrollChildrenJoins(Collections.singletonList(joinDescription)).forEach(j -> j.fetch(false));
                return nullPath;
            case 1:
                return candidatePaths.get(0);
            default: {
                // Multiple associations on parent -> try to find by the specified alias
                String targetFieldName = joinDescription.getOriginalAlias().toString();
                for (Path<?> candidatePath : candidatePaths) {
                    if (targetFieldName.equals(candidatePath.getMetadata().getElement())) {
                        return candidatePath;
                    }
                }
                throw new JoinerException("Join with ambiguous alias : " + joinDescription + ". Multiple mappings found: " + candidatePaths);
            }
        }
    }

    /**
     * Try to find attribute on a subclass of parent (for instance in test domain when looking for <code>Key</code> on <code>User</code>)
     *
     * @param parent         parent join or "from" element
     * @param targetType     type of joined attribute
     * @param candidatePaths container for found paths
     */
    private void findPathOnParentSubclasses(Path<?> parent, Class<?> targetType, List<Path<?>> candidatePaths) {
        for (Class<?> child : ReflectionUtils.getSubclasses(parent.getType(), entityManager)) {
            try {
                Class<?> clazz = getQClass(child);
                Object childInstance = ReflectionUtils.instantiate(clazz, parent.getMetadata().getElement());
                for (Field field : clazz.getFields()) {
                    testAliasCandidate(targetType, candidatePaths, getField(field, childInstance));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void testAliasCandidate(Class<?> targetType, List<Path<?>> candidatePaths, Object candidate) {
        if (candidate instanceof CollectionPathBase) {
            Class<?> elementType = (Class<?>) getField("elementType", candidate);

            if (elementType.isAssignableFrom(targetType)) {
                candidatePaths.add((Path<?>) candidate);
            }
        } else if (candidate instanceof EntityPathBase) {
            Class<?> type = ((EntityPathBase<?>) candidate).getType();
            if (type.isAssignableFrom(targetType)) {
                candidatePaths.add((Path<?>) candidate);
            }
        }
    }

    void clearCache() {
        aliasCache.clear();
    }

}
