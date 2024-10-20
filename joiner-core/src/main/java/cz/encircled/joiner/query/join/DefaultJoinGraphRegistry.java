package cz.encircled.joiner.query.join;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMap-based implementation of {@link JoinGraphRegistry}
 *
 * @author Vlad on 15-Aug-16.
 */
public class DefaultJoinGraphRegistry implements JoinGraphRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultJoinGraphRegistry.class);

    private final Map<Class<?>, Map<Object, List<JoinDescription>>> registry = new ConcurrentHashMap<>();

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
        Assert.notNull(name);
        Map<Object, List<JoinDescription>> joinsOfClass = registry.get(clazz);
        if (joinsOfClass != null) {
            List<JoinDescription> joins = joinsOfClass.get(name);
            if (joins != null) {
                return joins.stream().map(JoinDescription::copy).toList();
            }
        }

        log.warn(String.format("JoinGraph with name [%s] is not defined for class [%s]", name, clazz));
        return Collections.emptyList();
    }

    @Override
    public Map<Object, List<JoinDescription>> getAllJoinGraphs(Class<?> clazz) {
        return registry.getOrDefault(clazz, new HashMap<>());
    }

}
