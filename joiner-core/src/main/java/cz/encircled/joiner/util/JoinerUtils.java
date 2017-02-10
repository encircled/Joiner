package cz.encircled.joiner.util;

import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vlad on 29-Dec-16.
 */
public final class JoinerUtils {

    private JoinerUtils() {

    }

    public static List<Path<?>> collectPredicatePaths(Expression<?> expression) {
        List<Path<?>> result = new ArrayList<>();
        collectPredicatePathsInternal(expression, result);
        return result;
    }

    private static void collectPredicatePathsInternal(Expression<?> expression, List<Path<?>> paths) {
        if (expression instanceof Path) {
            paths.add((Path<?>) expression);
        } else if (expression instanceof Operation) {
            for (Expression exp : ((Operation<?>) expression).getArgs()) {
                collectPredicatePathsInternal(exp, paths);
            }
        }
    }

}
