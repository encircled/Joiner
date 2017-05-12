package cz.encircled.joiner.core;

import com.mysema.query.types.Operation;
import com.mysema.query.types.Path;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.query.join.JoinDescription;

import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation is responsible for predicate aliases lookup in a query
 *
 * @author Vlad on 10-Feb-17.
 */
public interface PredicateAliasResolver {

    /**
     * Resolves all correct paths used in specified <code>predicate</code>
     *
     * @param predicate   original predicate
     * @param joins       all query joins
     * @param usedAliases paths that are present in joins
     * @return rebuilt predicate
     * @see PredicateAliasResolver#resolvePath(Path, Map, Set)
     */
    Predicate resolvePredicate(Predicate predicate, List<JoinDescription> joins, Set<Path<?>> usedAliases);

    /**
     * Resolves all correct paths used in specified <code>operation</code>
     *
     * @param operation   original operation
     * @param joins       all query joins
     * @param usedAliases paths that are present in joins
     * @return rebuilt predicate
     * @see PredicateAliasResolver#resolvePath(Path, Map, Set)
     */
    Predicate resolveOperation(Operation<?> operation, List<JoinDescription> joins, Set<Path<?>> usedAliases);

    /**
     * If alias from <code>path</code> is not present in <code>usedAliases</code>, try to find it in joins.
     * If unambiguous alias is found, return rebuilt Path with corresponding alias.
     * <p>
     * It other words, it allows to use <b><code>QStatus.status.some...</code></b> instead of <b><code>J#path(QStatusParent.parent, QStatus.status</code></b>
     * (when target path is unambiguous, i.e. exactly one join with java type Status is present)
     * </p>
     *
     * @param path        original path
     * @param classToJoin java type to present joins
     * @param usedAliases paths that are present in joins
     * @param <T>         any
     * @return rebuilt or original path
     */
    <T> Path<T> resolvePath(Path<T> path, Map<AnnotatedElement, List<JoinDescription>> classToJoin, Set<Path<?>> usedAliases);
}
