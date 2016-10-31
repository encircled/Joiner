package cz.encircled.joiner.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Vlad on 04-Sep-16.
 */
public interface JoinerQuery<T, R> {

    EntityPath<T> getFrom();

    Expression<R> getReturnProjection(JPAQuery query);

    JoinerQueryBase<T, R> where(Predicate where);

    Predicate getWhere();

    JoinerQueryBase<T, R> distinct(boolean isDistinct);

    boolean isDistinct();

    JoinerQueryBase<T, R> groupBy(Expression<?> groupBy);

    Expression<?> getGroupBy();

    JoinerQueryBase<T, R> having(Predicate having);

    Predicate getHaving();

    /**
     * Add join graphs to the query.
     *
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
     * @param names names of join graphs
     * @return this
     */
    JoinerQueryBase<T, R> joinGraphs(String... names);

    /**
     * Add join graphs to the query.
     *
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
     * @param names names of join graphs
     * @return this
     */
    JoinerQueryBase<T, R> joinGraphs(Enum... names);

    /**
     * Add join graphs to the query.
     *
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
     * @param names names of join graphs
     * @return this
     */
    JoinerQueryBase<T, R> joinGraphs(Collection<?> names);

    Set<Object> getJoinGraphs();

    /**
     * Add <b>left</b> joins for specified paths
     *
     * @param paths join paths
     * @return this
     */
    JoinerQueryBase<T, R> joins(EntityPath<?>... paths);

    JoinerQueryBase<T, R> joins(JoinDescription... joins);

    JoinerQueryBase<T, R> joins(Collection<JoinDescription> joins);

    Collection<JoinDescription> getJoins();

    JoinDescription getJoin(Expression<?> expression);

    JoinerQueryBase<T, R> addHint(String hint, Object value);

    LinkedHashMap<String, List<Object>> getHints();

    JoinerQueryBase<T, R> addFeatures(QueryFeature... features);

    JoinerQueryBase<T, R> addFeatures(Collection<QueryFeature> features);

    List<QueryFeature> getFeatures();

    /**
     * Set offset for the query results
     *
     * @param offset value
     * @return this
     */
    JoinerQueryBase<T, R> offset(Long offset);

    Long getOffset();

    /**
     * Set max results for the query results
     *
     * @param limit value
     * @return this
     */
    JoinerQueryBase<T, R> limit(Long limit);

    Long getLimit();

    JoinerQueryBase<T, R> asc(Expression<?> orderBy);

    JoinerQueryBase<T, R> desc(Expression<?> orderBy);

    List<QueryOrder> getOrder();

    JoinerQuery<T, R> copy();

    boolean isCount();

}
