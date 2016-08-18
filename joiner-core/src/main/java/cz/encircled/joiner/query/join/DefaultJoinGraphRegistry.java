package cz.encircled.joiner.query.join;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ConcurrentHashMap-based implementation of {@link JoinGraphRegistry}
 *
 * @author Vlad on 15-Aug-16.
 */
public class DefaultJoinGraphRegistry implements JoinGraphRegistry {

    private final Map<Class, Map<String, List<JoinDescription>>> registry = new ConcurrentHashMap<>();

    @Override
    public void registerJoinGraph(String graphName, Collection<JoinDescription> joins, Class<?>... rootClasses) {
        Assert.notNull(graphName);
        Assert.notNull(joins);
        Assert.notNull(rootClasses);
        Assert.assertThat(rootClasses.length > 0);

        for (Class<?> clazz : rootClasses) {
            registry.computeIfAbsent(clazz, c -> new ConcurrentHashMap<>());
            Map<String, List<JoinDescription>> joinsOfClass = registry.get(clazz);
            if (joinsOfClass.containsKey(graphName)) {
                throw new JoinerException(String.format("JoinGraph with name [%s] is already defined for the class [%s]", graphName, clazz.getName()));
            } else {
                joinsOfClass.put(graphName, new ArrayList<>(joins));
            }
        }
    }

    @Override
    public List<JoinDescription> getJoinGraph(Class<?> clazz, String name) {
        Map<String, List<JoinDescription>> joinsOfClass = registry.get(clazz);
        if (joinsOfClass != null) {
            List<JoinDescription> joins = joinsOfClass.get(name);
            if (joins != null) {
                return joins;
            }
        }

        return Collections.emptyList();
    }

}
