package cz.encircled.joiner.core;

import static cz.encircled.joiner.util.ReflectionUtils.getField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.BooleanPath;
import com.mysema.query.types.path.CollectionPathBase;
import com.mysema.query.types.path.EntityPathBase;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.ReflectionUtils;

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
        while (!targetType.equals(Object.class)) {
            // TODO more efficient cache key
            String cacheKey = parent.getClass().getName() + parent.toString() + targetType.getSimpleName() + joinDescription.getOriginalAlias().toString();
            Path cached = aliasCache.get(cacheKey);
            if (cached != null && !cached.equals(nullPath)) {
                // TODO test
                // TODO optimize inheritance cases
                return cached;
            }

            List<Path<?>> candidatePaths = new ArrayList<>();

            for (Field field : parent.getClass().getFields()) {
                Object candidate = getField(field, parent);

                testAliasCandidate(targetType, candidatePaths, candidate);
            }

            if (candidatePaths.isEmpty()) {
                for (Class child : ReflectionUtils.getSubclasses(parent.getType(), entityManager)) {
                    Class<?> real;
                    Constructor<?> constructor;
                    try {
                        real = Class.forName(parent.getClass().getPackage().getName() + ".Q" + child.getSimpleName());
                        constructor = real.getConstructor(String.class);
                    } catch (Exception e) {
                        throw new RuntimeException();
                    }

                    Object childInstance;
                    try {
                        childInstance = constructor.newInstance(parent.getMetadata().getElement());
                    } catch (Exception e) {
                        // TODO when in subpackage
                        throw new RuntimeException();
                    }
                    for (Field field : real.getFields()) {

                        Object candidate = getField(field, childInstance);

                        testAliasCandidate(targetType, candidatePaths, candidate);
                    }
                }
            }

            if (candidatePaths.isEmpty()) {
                // TODO may be exception?
                J.unrollChildrenJoins(Collections.singletonList(joinDescription)).forEach(j -> j.fetch(false));

                aliasCache.put(cacheKey, nullPath);
                targetType = targetType.getSuperclass();
            } else if (candidatePaths.size() == 1) {
                aliasCache.put(cacheKey, candidatePaths.get(0));
                return candidatePaths.get(0);
            } else {
                // Multiple associations on parent, try find by specified alias
                String targetFieldName = joinDescription.getOriginalAlias().toString();
                for (Path<?> candidatePath : candidatePaths) {
                    if (targetFieldName.equals(candidatePath.getMetadata().getElement())) {
                        aliasCache.put(cacheKey, candidatePath);
                        return candidatePath;
                    }
                }
                // TODO add candidates to exception
                throw new JoinerException("Join with ambiguous alias : " + joinDescription + ". Multiple mappings found: " + candidatePaths);
            }
        }

        return null;
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
