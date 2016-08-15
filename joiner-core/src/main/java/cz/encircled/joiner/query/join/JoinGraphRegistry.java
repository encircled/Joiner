package cz.encircled.joiner.query.join;

import java.util.Collection;
import java.util.List;

/**
 * @author Vlad on 15-Aug-16.
 */
public interface JoinGraphRegistry {

    void registerJoinGraph(String graphName, Collection<JoinDescription> joins, Class<?>... rootClasses);

    List<JoinDescription> getJoinGraph(Class<?> clazz, String name);

}
