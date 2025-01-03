package cz.encircled.joiner.query;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CollectionPathBase;
import cz.encircled.joiner.query.join.JoinDescription;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Base interface which defines possible parameters of joiner query
 * T - select from
 * R - projection type
 *
 * @author Vlad on 04-Sep-16.
 */
public interface JoinerQuery<T, R> extends JoinRoot, SubQueryExpression<R> {

    EntityPath<T> getFrom();

    Expression<R> getReturnProjection();

    JoinerQuery<T, R> where(Predicate where);

    JoinerQuery<T, R> andWhere(BooleanExpression where);

    JoinerQuery<T, R> orWhere(BooleanExpression where);

    Predicate getWhere();

    JoinerQuery<T, R> distinct(boolean isDistinct);

    boolean isDistinct();

    JoinerQuery<T, R> groupBy(Path<?>... groupBy);

    Path<?>[] getGroupBy();

    JoinerQuery<T, R> having(Predicate having);

    Predicate getHaving();

    /**
     * Add join graphs to the query.
     *
     * @param names names of join graphs
     * @return this
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
     */
    JoinerQuery<T, R> joinGraphs(String... names);

    /**
     * Add join graphs to the query.
     *
     * @param names names of join graphs
     * @return this
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
     */
    JoinerQueryBase<T, R> joinGraphs(Enum... names);

    /**
     * Add join graphs to the query.
     *
     * @param names names of join graphs
     * @return this
     * @see cz.encircled.joiner.query.join.JoinGraphRegistry
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

    JoinerQueryBase<T, R> joins(CollectionPathBase<?, ?, ?>... path);

    JoinerQueryBase<T, R> joins(JoinDescription... joins);

    JoinerQueryBase<T, R> joins(Collection<JoinDescription> joins);

    Collection<JoinDescription> getJoins();

    JoinDescription removeJoin(JoinDescription join);

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
    JoinerQuery<T, R> offset(Long offset);

    Long getOffset();

    /**
     * Set max results for the query results
     *
     * @param limit value
     * @return this
     */
    JoinerQuery<T, R> limit(Long limit);

    Long getLimit();

    JoinerQuery<T, R> asc(Expression<?> orderBy);

    JoinerQuery<T, R> desc(Expression<?> orderBy);

    List<QueryOrder> getOrder();

    JoinerQuery<T, R> copy();

    JoinerQuery<T, Tuple> copy(Expression<?>[] newReturnProjections);

    boolean isCount();

    void setSubQueryMetadata(QueryMetadata metadata);

    Boolean isStatelessSession();

    JoinerQuery<T, R> setStatelessSession(Boolean isStatelessSession);

    JoinerQuery<T, R> useStatelessSession();

}
