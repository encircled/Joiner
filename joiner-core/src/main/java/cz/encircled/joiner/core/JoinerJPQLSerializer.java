package cz.encircled.joiner.core;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.types.*;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.Entity;

import java.util.*;

/**
 * Custom serializer for creating a JPQL string from a JoinerQuery.
 * This is a replacement for the JPQLSerializer from querydsl library.
 *
 * @author Encircled
 */
public class JoinerJPQLSerializer {

    private final StringBuilder query = new StringBuilder();
    private final List<Object> constants;
    private final Map<Object, String> constantToLabel = new HashMap<>();
    private final String constantPrefix = "a";

    public JoinerJPQLSerializer() {
        this(new ArrayList<>());
    }

    public JoinerJPQLSerializer(List<Object> constants) {
        this.constants = constants;
    }

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

        serializeJoinerQuery(joinerQuery, isCount);

        return query.toString();
    }

    /**
     * Helper method to serialize a JoinerQuery to a StringBuilder.
     * This is used by both the main serialize method and when serializing subqueries.
     *
     * @param joinerQuery the query to serialize
     * @param isCount     whether this is a count query
     */
    private void serializeJoinerQuery(JoinerQuery<?, ?> joinerQuery, boolean isCount) {
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
     * Serialize a subquery to a JPQL string.
     * This method reuses the serializeJoinerQuery method to avoid code duplication.
     * It also ensures that "fetch" is not used in subqueries, as it's not allowed.
     *
     * @param subQuery the subquery to serialize
     * @return the serialized subquery string
     */
    private String serializeSubQuery(JoinerQuery<?, ?> subQuery) {
        // Create a copy of the subquery with fetch set to false for all joins
        JoinerQuery<?, ?> subQueryCopy = subQuery.copy();
        for (JoinDescription join : subQueryCopy.getJoins()) {
            join.fetch(false);
            if (join.getChildren() != null) {
                for (JoinDescription nestedJoin : join.getChildren()) {
                    nestedJoin.fetch(false);
                }
            }
        }

        serializeJoinerQuery(subQueryCopy, subQueryCopy.isCount());
        return query.toString();
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
            if (projection instanceof FactoryExpressionBase) {
                List<Expression<?>> args = ((FactoryExpressionBase) projection).getArgs();
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) {
                        query.append(", ");
                    }
                    query.append(serializeExpression(args.get(i)));
                }

            } else {
                query.append(serializeExpression(projection));
            }
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
        boolean disableFetchJoins = joinerQuery.getReturnProjection() instanceof FactoryExpression<?> || joinerQuery.isCount();

        Collection<JoinDescription> joins = joinerQuery.getJoins();
        if (joins != null && !joins.isEmpty()) {
            for (JoinDescription join : joins) {
                appendJoin(join, disableFetchJoins);

                // Process nested joins recursively
                if (join.getChildren() != null) {
                    for (JoinDescription nestedJoin : join.getChildren()) {
                        appendJoin(nestedJoin, disableFetchJoins);
                    }
                }
            }
        }
    }

    private void appendJoin(JoinDescription join, boolean disableFetchJoins) {
        switch (join.getJoinType()) {
            case LEFTJOIN -> query.append(" left join ");
            case RIGHTJOIN -> query.append(" right join ");
            default -> query.append(" join ");
        }

        if (join.isFetch() && !disableFetchJoins) {
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
            // If the path is null, use the join's alias or a default value
            query.append(join.getAlias() != null ? join.getAlias().toString() : "unknown");
        }

        if (join.getAlias() != null) {
            query.append(" as ").append(join.getAlias());
        }
        if (join.getOn() != null) {
            query.append(" on ").append(serializeExpression(join.getOn()));
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
        if (joinerQuery.isCount()) return;

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
     * This implementation handles basic expressions and operations.
     *
     * @param expression the expression to serialize
     * @return the serialized expression
     */
    private String serializeExpression(Expression<?> expression) {
        return serializeExpression(expression, null);
    }

    /**
     * Serialize an expression to a JPQL string.
     * This implementation handles basic expressions and operations.
     *
     * @param expression the expression to serialize
     * @return the serialized expression
     */
    private String serializeExpression(Expression<?> expression, String constantOperator) {
        // For constants, we add them to the constants list and return a parameter placeholder
        if (expression instanceof com.querydsl.core.types.Constant) {
            Object constant = ((com.querydsl.core.types.Constant<?>) expression).getConstant();
            if (Objects.equals(constantOperator, "STRING_CONTAINS")) {
                constants.add("%" + constant + "%");
            } else if (Objects.equals(constantOperator, "STARTS_WITH")) {
                constants.add(constant + "%");
            } else {
                constants.add(constant);
            }
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

        // For subqueries, we serialize the subquery
        if (expression instanceof cz.encircled.joiner.query.JoinerQuery) {
            cz.encircled.joiner.query.JoinerQuery<?, ?> subQuery = (cz.encircled.joiner.query.JoinerQuery<?, ?>) expression;
            return "(" + new JoinerJPQLSerializer(constants).serializeSubQuery(subQuery) + ")";
        }

        // For operations, we handle the operator and operands
        if (expression instanceof Operation) {
            Operation<?> operation = (Operation<?>) expression;
            List<Expression<?>> args = operation.getArgs();
            String operator = operation.getOperator().toString();

            // Handle different types of operations
            if (args.size() == 2) {
                // Binary operation (e.g., a = b, a > b)
                String left = serializeExpression(args.get(0), operator);
                String right = serializeExpression(args.get(1), operator);

                // Special handling for common operators
                return switch (operator) {
                    case "EQ" -> left + " = " + right;
                    case "NE" -> left + " <> " + right;
                    case "GT" -> left + " > " + right;
                    case "GE" -> left + " >= " + right;
                    case "LT" -> left + " < " + right;
                    case "LE" -> left + " <= " + right;
                    case "STARTS_WITH", "STRING_CONTAINS" -> left + " like " + right;
                    default -> left + " " + operator.toLowerCase().replaceAll("_", " ") + " " + right;
                };
            } else if (args.size() == 1) {
                // Unary operation (e.g., not a)
                String arg = serializeExpression(args.get(0));

                // Special handling for common operators
                return switch (operator) {
                    case "NOT" -> "not " + arg;
                    case "IS_NULL" -> arg + " is null";
                    case "IS_NOT_NULL" -> arg + " is not null";
                    case "AVG_AGG" -> "avg(" + arg + ")";
                    case "COUNT_AGG" -> "count(" + arg + ")";
                    case "MAX_AGG" -> "max(" + arg + ")";
                    case "MIN_AGG" -> "min(" + arg + ")";
                    case "SUM_AGG" -> "sum(" + arg + ")";
                    default -> operator.toLowerCase() + "(" + arg + ")";
                };
            } else {
                // Function call or other operation
                StringBuilder sb = new StringBuilder();
                sb.append(operator.toLowerCase()).append("(");
                for (int i = 0; i < args.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(serializeExpression(args.get(i)));
                }
                sb.append(")");
                return sb.toString();
            }
        }

        // For other types of expressions, return the string representation
        return expression.toString();
    }
}
