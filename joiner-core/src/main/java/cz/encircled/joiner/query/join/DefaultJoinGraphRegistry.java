package cz.encircled.joiner.query.join;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ConcurrentHashMap-based implementation of {@link JoinGraphRegistry}
 *
 * @author Vlad on 15-Aug-16.
 */
public class DefaultJoinGraphRegistry implements JoinGraphRegistry {

    private final Map<Class, Map<Object, List<JoinDescription>>> registry = new ConcurrentHashMap<>();

    @Override
    public void registerJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses) {
        Assert.notNull(graphName);
        Assert.notNull(joins);
        Assert.notNull(rootClasses);
        Assert.assertThat(rootClasses.length > 0);

        for (Class<?> clazz : rootClasses) {
            registry.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
            Map<Object, List<JoinDescription>> joinsOfClass = registry.get(clazz);
            if (joinsOfClass.containsKey(graphName)) {
                throw new JoinerException(String.format("JoinGraph with name [%s] is already defined for the class [%s]", graphName, clazz.getName()));
            } else {
                joinsOfClass.put(graphName, new ArrayList<>(joins));
            }
        }
    }

    @Override
    public void replaceJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses) {
        Assert.notNull(graphName);
        Assert.notNull(joins);
        Assert.notNull(rootClasses);
        Assert.assertThat(rootClasses.length > 0);

        for (Class<?> clazz : rootClasses) {
            Map<Object, List<JoinDescription>> existing = registry.get(clazz);
            if (existing != null) {
                if (existing.containsKey(graphName)) {
                    existing.put(graphName, new ArrayList<>(joins));
                    continue;
                }
            }

            throw new JoinerException(String.format("JoinGraph with name [%s] is not defined for the class [%s]", graphName, clazz.getName()));
        }
    }

    @Override
    public void registerOrReplaceJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses) {
        Assert.notNull(graphName);
        Assert.notNull(joins);
        Assert.notNull(rootClasses);
        Assert.assertThat(rootClasses.length > 0);

        for (Class<?> clazz : rootClasses) {
            registry.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
            Map<Object, List<JoinDescription>> joinsOfClass = registry.get(clazz);
            joinsOfClass.put(graphName, new ArrayList<>(joins));
        }
    }

    @Override
    public List<JoinDescription> getJoinGraph(Class<?> clazz, Object name) {
        // TODO add tests for join description modifying
        Map<Object, List<JoinDescription>> joinsOfClass = registry.get(clazz);
        if (joinsOfClass != null) {
            List<JoinDescription> joins = joinsOfClass.get(name);
            if (joins != null) {
                return joins.stream().map(JoinDescription::copy).collect(Collectors.toList());
            }
        }

        throw new JoinerException(String.format("JoinGraph with name [%s] is not defined for class [%s]", name, clazz));
    }

    @Override
    public Map<Object, List<JoinDescription>> getAllJoinGraphs(Class<?> clazz) {
        return registry.getOrDefault(clazz, new HashMap<>());
    }

}
