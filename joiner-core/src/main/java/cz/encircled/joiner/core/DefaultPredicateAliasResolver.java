package cz.encircled.joiner.core;

import com.google.common.collect.ImmutableList;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Operation;
import com.querydsl.core.types.Operator;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.PathImpl;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.PredicateOperation;
import cz.encircled.joiner.query.join.JoinDescription;
import cz.encircled.joiner.util.ReflectionUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @see PredicateAliasResolver
 * @author Vlad on 10-Feb-17.
 */
// TODO find by original alias?
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
            return ReflectionUtils.instantiate(PredicateOperation.class, result.operator, ImmutableList.of(result.args.get(0), result.args.get(1)));
        } else {
            return ReflectionUtils.instantiate(PredicateOperation.class, result.operator, ImmutableList.of(result.args.get(0)));
        }
    }

    @Override
    public <T> Path<T> resolvePath(Path<T> path, Map<AnnotatedElement, List<JoinDescription>> classToJoin, Set<Path<?>> usedAliases) {
        if (!usedAliases.contains(path.getRoot())) {
            List<JoinDescription> candidates = classToJoin.get(path.getRoot().getAnnotatedElement());
            if (candidates != null && candidates.size() == 1) {
                PathImpl<?> resolvedRoot = ReflectionUtils.instantiate(PathImpl.class, candidates.get(0).getClass(), candidates.get(0).getAlias().getMetadata());
                return ReflectionUtils.instantiate(PathImpl.class, path.getType(), resolvedRoot, path.getMetadata().getElement());
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
                    newArg = ReflectionUtils
                            .instantiate(PredicateOperation.class, nestedResult.operator, ImmutableList.of(nestedResult.args.get(0), nestedResult.args.get(1)));
                } else {
                    newArg = ReflectionUtils.instantiate(PredicateOperation.class, nestedResult.operator, ImmutableList.of(nestedResult.args.get(0)));
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
        Operator operator;

        PredicateHolder(List<Expression<?>> args, Operator operator) {
            this.args = args;
            this.operator = operator;
        }
    }

}
