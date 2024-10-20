package cz.encircled.joiner.query.join;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
     * @param rootClasses target class for a new join graph
     */
    void registerJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses);

    /**
     * Adds new join graph to the registry or replace an existing one.
     *
     * @param graphName   graph unique name. Generally, any object may be used as a name, it should have correct hashCode method. String or enum is recommended.
     * @param joins       associated joins
     * @param rootClasses target class for a new join graph
     */
    void registerOrReplaceJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses);

    /**
     *
     * @param clazz target class
     * @param name join graph name
     * @return collection of registered join graphs
     */
    List<JoinDescription> getJoinGraph(Class<?> clazz, Object name);

    /**
     * @param clazz target class
     * @return map of all registered join graphs
     */
    Map<Object, List<JoinDescription>> getAllJoinGraphs(Class<?> clazz);

}
