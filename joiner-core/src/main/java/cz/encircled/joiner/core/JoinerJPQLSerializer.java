package cz.encircled.joiner.core;

import com.querydsl.core.types.*;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.QueryOrder;
import cz.encircled.joiner.query.join.J;
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
    private static final String constantPrefix = "a";

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
     * @return the serialized JPQL string
     */
    public String serialize(JoinerQuery<?, ?> joinerQuery) {
        query.setLength(0);
        constants.clear();
        constantToLabel.clear();

        serializeJoinerQuery(joinerQuery);

        return query.toString();
    }

    /**
     * Helper method to serialize a JoinerQuery to a StringBuilder.
     * This is used by both the main serialize method and when serializing subqueries.
     *
     * @param joinerQuery the query to serialize
     */
    private void serializeJoinerQuery(JoinerQuery<?, ?> joinerQuery) {
        if (joinerQuery.isCount()) {
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

        appendJoins(joinerQuery);
        appendWhere(joinerQuery);
        appendGroupBy(joinerQuery);
        appendHaving(joinerQuery);
        appendOrderBy(joinerQuery);
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
        for (JoinDescription join : J.unrollChildrenJoins(subQueryCopy.getJoins())) {
            join.fetch(false);
        }

        serializeJoinerQuery(subQueryCopy);
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
            case INNERJOIN -> query.append(" inner join ");
            case RIGHTJOIN -> query.append(" right join ");
            default -> query.append(" join ");
        }

        if (join.isFetch() && !disableFetchJoins && join.getOn() == null) {
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
            query.append(" ").append(join.getAlias());
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
            if (Objects.equals(constantOperator, "STRING_CONTAINS") || Objects.equals(constantOperator, "STRING_CONTAINS_IC")) {
                constants.add("%" + constant + "%");
            } else if (Objects.equals(constantOperator, "STARTS_WITH") || Objects.equals(constantOperator, "STARTS_WITH_IC")) {
                constants.add(constant + "%");
            } else if (Objects.equals(constantOperator, "ENDS_WITH") || Objects.equals(constantOperator, "ENDS_WITH_IC")) {
                constants.add("%" + constant);
            } else {
                constants.add(constant);
            }
            String label = constantPrefix + constants.size();
            constantToLabel.put(constants.get(constants.size() - 1), label);
            return "?" + constants.size();
        }

        // For paths, we return the path name
        if (expression instanceof Path<?> path) {
            if (path.getMetadata().getParent() != null) {
                return serializeExpression(path.getMetadata().getParent()) + "." + path.getMetadata().getName();
            }
            return path.getMetadata().getName();
        }

        // For subqueries, we serialize the subquery
        if (expression instanceof JoinerQuery<?, ?> subQuery) {
            return "(" + new JoinerJPQLSerializer(constants).serializeSubQuery(subQuery) + ")";
        }

        // For operations, we handle the operator and operands
        if (expression instanceof Operation<?> operation) {
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
                    case "STARTS_WITH", "STRING_CONTAINS", "STRING_CONTAINS_IC", "LIKE_IC", "LIKE", "ENDS_WITH_IC",
                         "ENDS_WITH", "STARTS_WITH_IC" -> left + " like " + right;
                    case "LIKE_ESCAPE", "LIKE_ESCAPE_IC" -> left + " like " + right + " escape '!'";
                    case "IN" -> {
                        // For IN operator, we need to check if the right operand is a subquery or if the constant is a collection
                        if (right.startsWith("(") && right.endsWith(")")) {
                            yield left + " in " + right;
                        } else if (right.startsWith("?")) {
                            int paramIndex = Integer.parseInt(right.substring(1));
                            Object constant = constants.get(paramIndex - 1);
                            if (constant instanceof Collection) {
                                yield left + " in " + right;
                            } else {
                                yield left + " = " + right;
                            }
                        } else {
                            yield left + " = " + right;
                        }
                    }
                    case "NOT_IN" -> {
                        // For NOT_IN operator, we need to check if the right operand is a subquery or if the constant is a collection
                        if (right.startsWith("(") && right.endsWith(")")) {
                            yield left + " not in " + right;
                        } else if (right.startsWith("?")) {
                            int paramIndex = Integer.parseInt(right.substring(1));
                            Object constant = constants.get(paramIndex - 1);
                            if (constant instanceof Collection) {
                                yield left + " not in " + right;
                            } else {
                                yield left + " <> " + right;
                            }
                        } else {
                            yield left + " <> " + right;
                        }
                    }
                    case "CONCAT" -> "concat(" + left + ", " + right + ")";
                    case "SUBSTR" -> "substring(" + left + ", " + right + ")";
                    case "UPPER" -> "upper(" + left + ")";
                    case "LOWER" -> "lower(" + left + ")";
                    case "TRIM" -> "trim(" + left + ")";
                    case "LENGTH" -> "length(" + left + ")";
                    case "LOCATE" -> "locate(" + left + ", " + right + ")";
                    case "ABS" -> "abs(" + left + ")";
                    case "SQRT" -> "sqrt(" + left + ")";
                    case "MOD" -> "mod(" + left + ", " + right + ")";
                    case "INDEX" -> "index(" + left + ")";
                    case "SIZE" -> "size(" + left + ")";
                    case "COALESCE" -> "coalesce(" + left + ", " + right + ")";
                    case "NULLIF" -> "nullif(" + left + ", " + right + ")";
                    case "CAST" -> "cast(" + left + " as " + right + ")";
                    case "TREAT" -> "treat(" + left + " as " + right + ")";
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
                    case "DISTINCT" -> "distinct " + arg;
                    case "ALL" -> "all " + arg;
                    case "ANY" -> "any " + arg;
                    case "SOME" -> "some " + arg;
                    case "EXISTS" -> "exists " + arg;
                    case "NOT_EXISTS" -> "not exists " + arg;
                    default -> operator.toLowerCase() + "(" + arg + ")";
                };
            } else {
                // Function call or other operations
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
