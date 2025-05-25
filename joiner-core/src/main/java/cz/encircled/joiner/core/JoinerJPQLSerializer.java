package cz.encircled.joiner.core;

import com.querydsl.core.JoinType;
import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom serializer for creating a JPQL string from a JoinerQuery.
 * This is a replacement for the JPQLSerializer from querydsl library.
 *
 * @author Encircled
 */
public class JoinerJPQLSerializer {

    private final StringBuilder query = new StringBuilder();
    private final List<Object> constants = new ArrayList<>();
    private final Map<Object, String> constantToLabel = new HashMap<>();
    private final String constantPrefix = "a";

    /**
     * Serialize the given query to a JPQL string.
     *
     * @param joinerQuery the query to serialize
     * @param isCount     whether this is a count query
     * @return the serialized JPQL string
     */
    public String serialize(JoinerQuery<?, ?> joinerQuery, boolean isCount) {
        query.setLength(0);
        constants.clear();
        constantToLabel.clear();

        if (isCount) {
            query.append("select count(");
            query.append(joinerQuery.getFrom().getMetadata().getName());
            query.append(") ");
        } else {
            query.append("select ");
            if (joinerQuery.isDistinct()) {
                query.append("distinct ");
            }
            appendProjection(joinerQuery);
            query.append(" ");
        }

        query.append("from ");
        appendFrom(joinerQuery);
        query.append(" ");

        appendJoins(joinerQuery);
        appendWhere(joinerQuery);
        appendGroupBy(joinerQuery);
        appendHaving(joinerQuery);
        appendOrderBy(joinerQuery);

        return query.toString();
    }

    /**
     * Serialize the given query metadata to a JPQL string.
     * This method is used as a replacement for querydsl's JPQLSerializer.serialize method.
     *
     * @param metadata   the query metadata to serialize
     * @param isCount    whether this is a count query
     * @param projection optional projection to use instead of the one in metadata
     * @return this serializer instance
     */
    public JoinerJPQLSerializer serialize(QueryMetadata metadata, boolean isCount, Expression<?> projection) {
        query.setLength(0);
        constants.clear();
        constantToLabel.clear();

        if (isCount) {
            query.append("select count(");
            if (metadata.getJoins().isEmpty()) {
                query.append("1");
            } else {
                query.append(metadata.getJoins().get(0).getTarget().toString());
            }
            query.append(") ");
        } else {
            query.append("select ");
            if (metadata.isDistinct()) {
                query.append("distinct ");
            }
            if (projection != null) {
                query.append(serializeExpression(projection));
            } else if (metadata.getProjection() != null) {
                query.append(serializeExpression(metadata.getProjection()));
            } else if (!metadata.getJoins().isEmpty()) {
                query.append(metadata.getJoins().get(0).getTarget().toString());
            } else {
                query.append("1");
            }
            query.append(" ");
        }

        if (!metadata.getJoins().isEmpty()) {
            query.append("from ");
            for (int i = 0; i < metadata.getJoins().size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(getEntityName(metadata.getJoins().get(i).getTarget().getType()))
                        .append(" ")
                        .append(metadata.getJoins().get(i).getTarget().toString());
            }
            query.append(" ");
        }

        if (metadata.getWhere() != null) {
            query.append("where ")
                    .append(serializeExpression(metadata.getWhere()))
                    .append(" ");
        }

        if (!metadata.getGroupBy().isEmpty()) {
            query.append("group by ");
            for (int i = 0; i < metadata.getGroupBy().size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(metadata.getGroupBy().get(i)));
            }
            query.append(" ");
        }

        if (metadata.getHaving() != null) {
            query.append("having ")
                    .append(serializeExpression(metadata.getHaving()))
                    .append(" ");
        }

        if (!metadata.getOrderBy().isEmpty()) {
            query.append("order by ");
            for (int i = 0; i < metadata.getOrderBy().size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(metadata.getOrderBy().get(i).getTarget()))
                        .append(metadata.getOrderBy().get(i).isAscending() ? " asc" : " desc");
            }
            query.append(" ");
        }

        return this;
    }

    /**
     * Get the list of constants used in the query.
     *
     * @return the list of constants
     */
    public List<Object> getConstants() {
        return constants;
    }

    /**
     * Get the map of constants to their labels.
     *
     * @return the map of constants to labels
     */
    public Map<Object, String> getConstantToLabel() {
        return constantToLabel;
    }

    @Override
    public String toString() {
        return query.toString();
    }

    private void appendProjection(JoinerQuery<?, ?> joinerQuery) {
        Expression<?> projection = joinerQuery.getReturnProjection();
        if (projection != null) {
            query.append(serializeExpression(projection));
        } else {
            query.append(joinerQuery.getFrom().getMetadata().getName());
        }
    }

    private String getEntityName(Class<?> clazz) {
        Entity entityAnnotation = clazz.getAnnotation(Entity.class);
        if (entityAnnotation != null && !entityAnnotation.name().isEmpty()) {
            return entityAnnotation.name();
        } else if (clazz.getPackage() != null && !clazz.getPackage().getName().isEmpty()) {
            String pn = clazz.getPackage().getName();
            return clazz.getName().substring(pn.length() + 1);
        } else {
            return clazz.getName();
        }
    }

    private void appendFrom(JoinerQuery<?, ?> joinerQuery) {
        query.append(getEntityName(joinerQuery.getFrom().getType()))
                .append(" ")
                .append(joinerQuery.getFrom().getMetadata().getName());
    }

    private void appendJoins(JoinerQuery<?, ?> joinerQuery) {
        Collection<JoinDescription> joins = joinerQuery.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (JoinDescription join : joins) {
                query.append(join.getJoinType() == JoinType.LEFTJOIN ? " left join " : " join ");
                if (join.isFetch()) {
                    query.append("fetch ");
                }

                // Handle different path types
                Expression<?> path = null;
                if (join.isCollectionPath()) {
                    path = join.getCollectionPath();
                } else {
                    path = join.getSingularPath();
                }

                if (path != null) {
                    query.append(serializeExpression(path));
                } else {
                    // If path is null, use the join's alias or a default value
                    query.append(join.getAlias() != null ? join.getAlias().toString() : "unknown");
                }

                if (join.getAlias() != null) {
                    query.append(" as ").append(join.getAlias());
                }
                if (join.getOn() != null) {
                    query.append(" on ").append(serializeExpression(join.getOn()));
                }
            }
        }
    }

    private void appendWhere(JoinerQuery<?, ?> joinerQuery) {
        Predicate where = joinerQuery.getWhere();
        if (where != null) {
            query.append(" where ").append(serializeExpression(where));
        }
    }

    private void appendGroupBy(JoinerQuery<?, ?> joinerQuery) {
        Path<?>[] groupBy = joinerQuery.getGroupBy();
        if (groupBy != null && groupBy.length > 0) {
            query.append(" group by ");
            for (int i = 0; i < groupBy.length; i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(groupBy[i]));
            }
        }
    }

    private void appendHaving(JoinerQuery<?, ?> joinerQuery) {
        Predicate having = joinerQuery.getHaving();
        if (having != null) {
            query.append(" having ").append(serializeExpression(having));
        }
    }

    private void appendOrderBy(JoinerQuery<?, ?> joinerQuery) {
        List<QueryOrder> orders = joinerQuery.getOrder();
        if (orders != null && !orders.isEmpty()) {
            query.append(" order by ");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                QueryOrder order = orders.get(i);
                query.append(serializeExpression(order.getTarget()));
                query.append(order.isAsc() ? " asc" : " desc");
            }
        }
    }

    /**
     * Serialize an expression to a JPQL string.
     * This is a simplified implementation that handles basic expressions.
     * For more complex expressions, additional handling would be needed.
     *
     * @param expression the expression to serialize
     * @return the serialized expression
     */
    private String serializeExpression(Expression<?> expression) {
        // This is a simplified implementation
        // In a real implementation, you would need to handle different types of expressions
        // such as operations, constants, paths, etc.

        // For constants, we add them to the constants list and return a parameter placeholder
        if (expression instanceof com.querydsl.core.types.Constant) {
            Object constant = ((com.querydsl.core.types.Constant<?>) expression).getConstant();
            constants.add(constant);
            String label = constantPrefix + constants.size();
            constantToLabel.put(constant, label);
            return "?" + constants.size();
        }

        // For paths, we return the path name
        if (expression instanceof Path) {
            Path<?> path = (Path<?>) expression;
            if (path.getMetadata().getParent() != null) {
                return serializeExpression(path.getMetadata().getParent()) + "." + path.getMetadata().getName();
            }
            return path.getMetadata().getName();
        }

        // For operations, we would need to handle the operator and operands
        // This is a simplified version that just returns the string representation
        return expression.toString();
    }
}
