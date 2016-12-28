package cz.encircled.joiner.query;

import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.Assert;

import java.util.Map;

/**
 * Indicates that an object contains joins
 *
 * @author Vlad on 27-Oct-16.
 */
public interface JoinRoot {

    default JoinDescription getJoin(Expression<?> expression) {
        Assert.notNull(expression);

        return getAllJoins().get(expression.toString());
    }

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
