package cz.encircled.joiner.core;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPathBase;
import com.mysema.query.types.path.EntityPathBase;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.ReflectionUtils;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cz.encircled.joiner.util.ReflectionUtils.getField;

/**
 * @author Vlad on 16-Aug-16.
 */
public class DefaultAliasResolver implements AliasResolver {

    private static final Path<?> nullPath = new BooleanPath("");

    private final EntityManager entityManager;

    private final Map<String, Path> aliasCache = new ConcurrentHashMap<>();

    public DefaultAliasResolver(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void resolveJoinAlias(JoinDescription join, EntityPath<?> root) {
        Path<?> parent = join.getParent() != null ? join.getParent().getAlias() : root;

        Path<?> fieldOnParent = findPathOnParent(parent, join.getAlias().getType(), join);
        if (fieldOnParent instanceof CollectionPathBase) {
            join.collectionPath((CollectionPathBase<?, ?, ?>) fieldOnParent);
        } else if (fieldOnParent instanceof EntityPath) {
            join.singlePath((EntityPath<?>) fieldOnParent);
        } else {
            throw new JoinerException("Target field not found for join " + join);
        }
    }

    private Path<?> findPathOnParent(Path<?> parent, Class<?> targetType, JoinDescription joinDescription) {
        // TODO more efficient cache key
        String cacheKey = parent.getClass().getName() + parent.toString() + targetType.getSimpleName() + joinDescription.getOriginalAlias().toString();
        if (aliasCache.containsKey(cacheKey)) {
            return aliasCache.get(cacheKey);
        }

        Path<?> result = doFindPathOnParent(parent, targetType, joinDescription);
        aliasCache.put(cacheKey, result);
        return result;
    }

    protected Path<?> doFindPathOnParent(Path<?> parent, Class<?> targetType, JoinDescription joinDescription) {
        List<Path<?>> candidatePaths = new ArrayList<>();

        for (Field field : parent.getClass().getFields()) {
            testAliasCandidate(targetType, candidatePaths, getField(field, parent));
        }

        if (candidatePaths.isEmpty()) {
            for (Class child : ReflectionUtils.getSubclasses(parent.getType(), entityManager)) {
                try {
                    Class clazz = Class.forName(child.getPackage().getName() + ".Q" + child.getSimpleName());
                    Object childInstance = ReflectionUtils.instantiate(clazz, (String) parent.getMetadata().getElement());
                    for (Field field : clazz.getFields()) {
                        testAliasCandidate(targetType, candidatePaths, getField(field, childInstance));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (candidatePaths.isEmpty()) {
            J.unrollChildrenJoins(Collections.singletonList(joinDescription)).forEach(j -> j.fetch(false));
            return nullPath;
        } else if (candidatePaths.size() == 1) {
            return candidatePaths.get(0);
        } else {
            // Multiple associations on parent, try find by specified alias
            String targetFieldName = joinDescription.getOriginalAlias().toString();
            for (Path<?> candidatePath : candidatePaths) {
                if (targetFieldName.equals(candidatePath.getMetadata().getElement())) {
                    return candidatePath;
                }
            }
            // TODO add candidates to exception
            throw new JoinerException("Join with ambiguous alias : " + joinDescription + ". Multiple mappings found: " + candidatePaths);
        }
    }

    private void testAliasCandidate(Class<?> targetType, List<Path<?>> candidatePaths, Object candidate) {
        if (candidate instanceof CollectionPathBase) {
            Field elementTypeField = ReflectionUtils.findField(candidate.getClass(), "elementType");
            Class<?> elementType = (Class<?>) getField(elementTypeField, candidate);

            if (elementType.isAssignableFrom(targetType)) {
                candidatePaths.add((Path<?>) candidate);
            }
        } else if (candidate instanceof EntityPathBase) {
            Class<?> type = ((EntityPathBase) candidate).getType();
            if (type.isAssignableFrom(targetType)) {
                candidatePaths.add((Path<?>) candidate);
            }
        }
    }

}
