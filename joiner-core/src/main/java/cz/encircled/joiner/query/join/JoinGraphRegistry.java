package cz.encircled.joiner.query.join;

import java.util.Collection;
import java.util.List;

/**
 * JoinGraph allows to predefine a set of joins for a specific class that can be added to a query using {@link cz.encircled.joiner.query.JoinerQuery#joinGraphs(String...)}
 *
 * @author Vlad on 15-Aug-16.
 */
public interface JoinGraphRegistry {

    void registerJoinGraph(Object graphName, Collection<JoinDescription> joins, Class<?>... rootClasses);

    List<JoinDescription> getJoinGraph(Class<?> clazz, Object name);

}
