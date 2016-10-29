package cz.encircled.joiner.query.join;

import java.util.Collection;
import java.util.List;

/**
 * JoinGraph allows to predefine a set of joins for a specific class that can be added to a query using {@link cz.encircled.joiner.query.JoinerQuery#joinGraphs(String...)}
 *
 * @author Vlad on 15-Aug-16.
 */
public interface JoinGraphRegistry {

    /**
     * Adds new join graph to the registry.
     *
     * @param graphName graph unique name. Generally, any object may be used as a name, it should have correct hashCode method. String or enum is recommended.
     * @param joins associated joins
     * @param rootClasses target class for new join graph
     */
    void registerJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses);

    /**
     *
     * @param clazz target class
     * @param name join graph name
     * @return collection of registered join graphs
     */
    List<JoinDescription> getJoinGraph(Class<?> clazz, Object name);

}
