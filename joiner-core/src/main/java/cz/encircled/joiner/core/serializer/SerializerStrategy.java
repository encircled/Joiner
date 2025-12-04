package cz.encircled.joiner.core.serializer;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpression;
import com.querydsl.core.types.Operation;
import cz.encircled.joiner.query.CollectionJoinerQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.query.join.JoinDescription;

import java.util.Collection;
import java.util.List;

abstract class SerializerStrategy implements JoinerSerializer {

    protected final StringBuilder query = new StringBuilder();
    protected final List<Object> constants;

    public SerializerStrategy(List<Object> constants) {
        this.constants = constants;
    }

    @Override
    public String serialize(JoinerQuery<?, ?> joinerQuery) {
        query.setLength(0);
        constants.clear();

        serializeJoinerQuery(joinerQuery);

        return query.toString();
    }

    /**
     * Helper method to serialize a JoinerQuery to a StringBuilder.
     * This is used by both the main serialize method and when serializing subqueries.
     *
     * @param joinerQuery the query to serialize
     */
    protected void serializeJoinerQuery(JoinerQuery<?, ?> joinerQuery) {
        query.append("select ");
        if (joinerQuery.isCount()) {
            if (joinerQuery.getReturnProjection() instanceof Operation) {
                query.append(serializeExpression(joinerQuery.getReturnProjection(), null));
                query.append(" ");
            } else {
                appendCount(joinerQuery);
            }
        } else if(joinerQuery instanceof CollectionJoinerQuery<?,?>) {
            // Used for 'exists' subqueries
            query.append("1 ");
        } else {
            if (joinerQuery.isDistinct()) {
                query.append("distinct ");
            }
            appendProjection(joinerQuery);
            query.append(" ");
        }

        query.append("from ");
        if (joinerQuery instanceof CollectionJoinerQuery<?,?> collection) {
            appendFromCollection(collection);
        } else {
            appendFrom(joinerQuery);
        }

        appendJoins(joinerQuery);
        appendWhere(joinerQuery);
        appendGroupBy(joinerQuery);
        appendHaving(joinerQuery);
        appendOrderBy(joinerQuery);
    }

    protected void appendJoins(JoinerQuery<?, ?> joinerQuery) {
        Collection<JoinDescription> joins = joinerQuery.getJoins();
        if (joins != null) {
            for (JoinDescription join : joins) {
                appendJoin(joinerQuery, join);
                if (join.getChildren() != null) {
                    for (JoinDescription nestedJoin : join.getChildren()) {
                        appendJoin(joinerQuery, nestedJoin);
                    }
                }
            }
        }
    }

    protected void appendGroupBy(JoinerQuery<?, ?> joinerQuery) {
        List<Expression<?>> groupBy = joinerQuery.getGroupBy();
        if (groupBy != null && !groupBy.isEmpty()) {
            query.append(" group by ");
            for (int i = 0; i < groupBy.size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(groupBy.get(i), null));
            }
        }
    }

    protected void appendOrderBy(JoinerQuery<?, ?> joinerQuery) {
        if (joinerQuery.isCount()) return;

        List<QueryOrder> orders = joinerQuery.getOrder();
        if (orders != null && !orders.isEmpty()) {
            query.append(" order by ");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                QueryOrder order = orders.get(i);
                query.append(serializeExpression(order.getTarget(), null));
                query.append(order.isAsc() ? " asc" : " desc");
            }
        }
    }

    abstract void appendFromCollection(CollectionJoinerQuery<?,?> collection);
    abstract void appendCount(JoinerQuery<?, ?> joinerQuery);
    abstract void appendFrom(JoinerQuery<?, ?> joinerQuery);
    abstract void appendProjection(JoinerQuery<?, ?> joinerQuery);
    abstract void appendJoin(JoinerQuery<?, ?> joinerQuery, JoinDescription join);
    abstract void appendWhere(JoinerQuery<?, ?> joinerQuery);
    abstract void appendHaving(JoinerQuery<?, ?> joinerQuery);

    abstract String serializeExpression(Expression<?> expression, String parentOpOperator);

}
