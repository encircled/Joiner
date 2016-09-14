package cz.encircled.joiner.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import com.mysema.query.types.Predicate;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * @author Vlad on 04-Sep-16.
 */
public interface JoinerQuery<T, R> {

    Expression<R> getReturnProjection(JPAQuery query);

    Predicate getWhere();

    JoinerQueryBase<T, R> distinct(boolean isDistinct);

    JoinerQueryBase<T, R> groupBy(Expression<?> groupBy);

    Expression<?> getGroupBy();

    boolean isDistinct();

    JoinerQueryBase<T, R> where(Predicate where);

    JoinerQueryBase<T, R> having(Predicate having);

    Predicate getHaving();

    EntityPath<T> getFrom();

    List<String> getJoinGraphs();

    Collection<JoinDescription> getJoins();

    JoinDescription getJoin(Expression<?> expression);

    JoinerQueryBase<T, R> joinGraphs(String... names);

    JoinerQueryBase<T, R> joins(JoinDescription... joins);

    JoinerQueryBase<T, R> joins(Collection<JoinDescription> joins);

    JoinerQueryBase<T, R> addHint(String hint, Object value);

    JoinerQueryBase<T, R> addFeatures(QueryFeature... features);

    List<QueryFeature> getFeatures();

    LinkedHashMap<String, List<Object>> getHints();
}
