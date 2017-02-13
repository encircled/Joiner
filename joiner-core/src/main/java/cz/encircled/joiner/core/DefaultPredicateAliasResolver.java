package cz.encircled.joiner.core;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.mysema.query.BooleanBuilder;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Operation;
import com.mysema.query.types.Operator;
import com.mysema.query.types.Path;
import com.mysema.query.types.PathImpl;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.PredicateOperation;
import com.mysema.query.types.expr.BooleanOperation;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Vlad on 10-Feb-17.
 */
public class DefaultPredicateAliasResolver implements PredicateAliasResolver {

    @Override
    public Predicate resolvePredicate(Predicate predicate, List<JoinDescription> joins, Set<Path<?>> usedAliases) {
        if (predicate instanceof BooleanBuilder) {
            return resolveOperation((Operation<?>) ((BooleanBuilder) predicate).getValue(), joins, usedAliases);
        } else {
            return resolveOperation((Operation<?>) predicate, joins, usedAliases);
        }
    }

    @Override
    public Predicate resolveOperation(Operation<?> operation, List<JoinDescription> joins, Set<Path<?>> usedAliases) {
        Map<AnnotatedElement, List<JoinDescription>> collect = joins.stream()
                .collect(Collectors.groupingBy(j -> j.getOriginalAlias().getAnnotatedElement()));

        PredicateHolder result = rebuildPredicate(new PredicateHolder(operation.getArgs(), operation.getOperator()), collect, usedAliases);

        if (result.args.size() == 2) {
            return BooleanOperation.create(result.operator, result.args.get(0), result.args.get(1));
        } else {
            return BooleanOperation.create(result.operator, result.args.get(0));
        }
    }

    @Override
    public <T> Path<T> resolvePath(Path<T> path, Map<AnnotatedElement, List<JoinDescription>> classToJoin, Set<Path<?>> usedAliases) {
        if (!usedAliases.contains(path.getRoot())) {
            List<JoinDescription> candidates = classToJoin.get(path.getRoot().getAnnotatedElement());
            if (candidates != null && candidates.size() == 1) {
                PathImpl<?> resolvedRoot = new PathImpl<>(candidates.get(0).getClass(), candidates.get(0).getAlias().getMetadata());
                return new PathImpl<>(path.getType(), resolvedRoot, (String) path.getMetadata().getElement());
            }
        }
        return path;
    }

    private PredicateHolder rebuildPredicate(PredicateHolder predicateHolder, Map<AnnotatedElement, List<JoinDescription>> classToJoin,
            Set<Path<?>> usedAliases) {
        PredicateHolder result = new PredicateHolder(new ArrayList<>(), predicateHolder.operator);

        for (Expression<?> arg : predicateHolder.args) {
            Expression<?> newArg = arg;
            if (arg instanceof PredicateOperation) {
                // Recursively rebuild predicates
                PredicateOperation casted = (PredicateOperation) arg;
                PredicateHolder nestedParams = new PredicateHolder(casted.getArgs(), casted.getOperator());
                PredicateHolder nestedResult = rebuildPredicate(nestedParams, classToJoin, usedAliases);

                if (nestedResult.args.size() == 2) {
                    newArg = PredicateOperation.create(nestedResult.operator, nestedResult.args.get(0), nestedResult.args.get(1));
                } else {
                    newArg = PredicateOperation.create(nestedResult.operator, nestedResult.args.get(0));
                }
            } else if (arg instanceof Path) {
                newArg = resolvePath((Path<?>) arg, classToJoin, usedAliases);
            }

            result.args.add(newArg);
        }

        return result;
    }

    private static class PredicateHolder {
        List<Expression<?>> args;
        Operator<Boolean> operator;

        PredicateHolder(List<Expression<?>> args, Operator<?> operator) {
            this.args = args;
            this.operator = (Operator<Boolean>) operator;
        }
    }

}
