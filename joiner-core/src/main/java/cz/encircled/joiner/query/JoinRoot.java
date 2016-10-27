package cz.encircled.joiner.query;

import cz.encircled.joiner.query.join.JoinDescription;

import java.util.Map;

/**
 * Indicates that an object contains joins
 *
 * @author Vlad on 27-Oct-16.
 */
public interface JoinRoot {

    Map<String, JoinDescription> getAllJoins();

    default void addJoin(JoinDescription join) {
        String key = join.getOriginalAlias().toString();
        JoinDescription present = getAllJoins().get(key);
        if (present == null) {
            getAllJoins().put(key, join);
        } else {
            for (JoinDescription joinDescription : join.getChildren()) {
                present.nested(joinDescription);
            }
        }
    }

}
