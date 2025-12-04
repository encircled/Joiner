package cz.encircled.joiner.core.serializer;

import com.querydsl.core.support.Context;
import com.querydsl.core.types.*;
import cz.encircled.joiner.core.converter.JPACollectionAnyVisitor;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.CollectionJoinerQuery;
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
public class JoinerJPQLSerializer extends SerializerStrategy {

    public JoinerJPQLSerializer() {
        this(new ArrayList<>());
    }

    public JoinerJPQLSerializer(List<Object> constants) {
        super(constants);
    }

    /**
     * Serialize the given query to a JPQL string.
     *
     * @param joinerQuery the query to serialize
     * @return the serialized JPQL string
     */
    @Override
    public String serialize(JoinerQuery<?, ?> joinerQuery) {
        query.setLength(0);
        constants.clear();

        serializeJoinerQuery(joinerQuery);

        return query.toString();
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
    @Override
    public List<Object> getConstants() {
        return constants;
    }

    @Override
    protected void appendProjection(JoinerQuery<?, ?> joinerQuery) {
        Expression<?> projection = joinerQuery.getReturnProjection();
        if (projection instanceof FactoryExpressionBase<?> p) {
            List<Expression<?>> args = p.getArgs();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(args.get(i)));
            }

        } else {
            query.append(serializeExpression(projection));
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

    @Override
    protected void appendFromCollection(CollectionJoinerQuery<?, ?> joinerQuery) {
        query.append(joinerQuery.getFromCollection().toString())
                .append(" ")
                .append(joinerQuery.getReturnProjection());
    }

    @Override
    protected void appendFrom(JoinerQuery<?, ?> joinerQuery) {
        query.append(getEntityName(joinerQuery.getFrom().getType()))
                .append(" ")
                .append(joinerQuery.getFrom().getMetadata().getName());
    }

    @Override
    protected void appendJoin(JoinerQuery<?, ?> joinerQuery, JoinDescription join) {
        boolean disableFetchJoins = joinerQuery.getReturnProjection() instanceof FactoryExpression<?> || joinerQuery.isCount();

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
        Expression<?> path;
        if (join.isCollectionPath()) {
            path = join.getCollectionPath();
        } else {
            path = join.getSingularPath();
        }
        // May happen if the query was not processed by alias resolver, in a toString() for example
        if (path == null) {
            path = join.getPath();
        }

        if (path != null) {
            query.append(serializeExpression(path));
        } else {
            if (join.getAlias() == null) {
                throw new JoinerException("Join path is null");
            }
            query.append(join.getAlias().toString());
        }

        if (join.getAlias() != null) {
            query.append(" ").append(join.getAlias());
        }
        if (join.getOn() != null) {
            query.append(" on ").append(serializeExpression(join.getOn()));
        }
    }

    @Override
    protected void appendWhere(JoinerQuery<?, ?> joinerQuery) {
        if (joinerQuery.getWhere() != null) {
            Expression<?> where = joinerQuery.getWhere().accept(new JPACollectionAnyVisitor(), new Context());
            query.append(" where ").append(serializeExpression(where));
        }
    }

    @Override
    protected void appendHaving(JoinerQuery<?, ?> joinerQuery) {
        Predicate having = joinerQuery.getHaving();
        if (having != null) {
            query.append(" having ").append(serializeExpression(having));
        }
    }

    @Override
    void appendCount(JoinerQuery<?, ?> joinerQuery) {
        query.append("count(");
        query.append(joinerQuery.getFrom().getMetadata().getName());
        query.append(") ");
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
    @Override
    protected String serializeExpression(Expression<?> expression, String parentOpOperator) {
        // For constants, we add them to the constants list and return a parameter placeholder
        if (expression instanceof com.querydsl.core.types.Constant) {
            Object constant = ((com.querydsl.core.types.Constant<?>) expression).getConstant();
            switch (parentOpOperator) {
                case "STRING_CONTAINS" -> constants.add("%" + constant + "%");
                case "STRING_CONTAINS_IC" -> constants.add("%" + lowered(constant) + "%");
                case "STARTS_WITH" -> constants.add(constant + "%");
                case "STARTS_WITH_IC" -> constants.add(lowered(constant) + "%");
                case "ENDS_WITH" -> constants.add("%" + constant);
                case "ENDS_WITH_IC" -> constants.add("%" + lowered(constant));
                case "LIKE_IC" -> constants.add(lowered(constant));
                default -> constants.add(constant);
            }
            return "?" + constants.size();
        }

        // For paths, we return the path name
        if (expression instanceof Path<?> path) {
            Path<?> parent = path.getMetadata().getParent();
            if (parent != null) {
                return serializeExpression(parent) + "." + path.getMetadata().getName();
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
            if (args.size() == 3) {
                String left = serializeExpression(args.get(0), operator);
                String middle = serializeExpression(args.get(1), operator);
                String right = serializeExpression(args.get(2), operator);

                return switch (operator) {
                    case "SUBSTR_2ARGS" -> "substring(" + left + ", " + middle + ", " + right + ")";
                    case "BETWEEN" -> left + " between " + middle + " and " + right;
                    default -> throw new JoinerException("Unsupported operator: " + operator);
                };
            } else if (args.size() == 2) {
                // Binary operation (e.g., a = b, a > b)
                String left = serializeExpression(args.get(0), operator);
                String right = serializeExpression(args.get(1), operator);

                boolean isConditional = operation.getOperator() == Ops.AND || operation.getOperator() == Ops.OR;
                boolean isConstantConditional = "AND".equals(parentOpOperator) || "OR".equals(parentOpOperator);
                boolean addParentheses = isConditional && isConstantConditional;

                // Special handling for common operators
                String result = serializeOperation(operator, left, right);
                return addParentheses ? "(" + result + ")" : result;
            } else if (args.size() == 1) {
                // Unary operation (e.g., not a)
                String arg = serializeExpression(args.get(0));

                // Special handling for common operators
                return switch (operator) {
                    case "COL_SIZE" -> "size(" + arg + ")";
                    case "COL_IS_EMPTY" -> arg + " is empty";
                    case "STRING_IS_EMPTY" -> "length(" + arg + ") = 0";
                    case "STRING_LENGTH" -> "length(" + arg + ")";
                    case "COALESCE" -> "coalesce" + arg;
                    case "NOT" -> "not " + arg;
                    case "IS_NULL" -> arg + " is null";
                    case "IS_NOT_NULL" -> arg + " is not null";
                    case "AVG_AGG" -> "avg(" + arg + ")";
                    case "COUNT_AGG" -> "count(" + arg + ")";
                    case "COUNT_DISTINCT_AGG" -> "count(distinct " + arg + ")";
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
                throw new JoinerException("Unsupported operator: " + operator);
            }
        }

        return expression.toString();
    }

    private static String serializeOperation(String operator, String left, String right) {
        return switch (operator) {
            case "EQ" -> left + " = " + right;
            case "NE" -> left + " <> " + right;
            case "GT" -> left + " > " + right;
            case "GOE" -> left + " >= " + right;
            case "LT" -> left + " < " + right;
            case "LOE" -> left + " <= " + right;
            case "LIKE_IC", "ENDS_WITH_IC", "STARTS_WITH_IC", "STRING_CONTAINS_IC" ->
                    "lower(" + left + ") like " + right;
            case "STARTS_WITH", "STRING_CONTAINS", "LIKE", "ENDS_WITH" -> left + " like " + right;
            case "LIKE_ESCAPE", "LIKE_ESCAPE_IC" -> left + " like " + right + " escape '!'";
            case "IN" -> left + " in " + right;
            case "NOT_IN" -> left + " not in " + right;
            case "CONCAT" -> "concat(" + left + ", " + right + ")";
            case "SUBSTR_1ARG" -> "substring(" + left + ", " + right + ")";
            case "LOCATE" -> "locate(" + left + ", " + right + ")";
            case "MOD" -> "mod(" + left + ", " + right + ")";
            case "COALESCE" -> "coalesce(" + left + ", " + right + ")";
            case "NULLIF" -> "nullif(" + left + ", " + right + ")";
            case "CAST" -> "cast(" + left + " as " + right + ")";
            case "TREAT" -> "treat(" + left + " as " + right + ")";
            case "LIST" -> "(" + left + ", " + right + ")";
            default -> left + " " + operator.toLowerCase().replaceAll("_", " ") + " " + right;
        };
    }

    private static String lowered(Object constant) {
        if (constant != null) {
            return constant.toString().toLowerCase();
        }
        return null;
    }

}
