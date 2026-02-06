package cz.encircled.joiner.core.serializer;

import com.querydsl.core.support.Context;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.CollectionExpressionBase;
import com.querydsl.core.types.dsl.CollectionPathBase;
import cz.encircled.joiner.core.converter.JPACollectionAnyVisitor;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.CollectionJoinerQuery;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.J;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JoinerSQLSerializer extends SerializerStrategy {

    private final SqlNamingStrategy namingStrategy;

    public JoinerSQLSerializer(SqlNamingStrategy namingStrategy) {
        this(namingStrategy, new ArrayList<>());
    }

    public JoinerSQLSerializer(SqlNamingStrategy namingStrategy, List<Object> constants) {
        super(constants);
        this.namingStrategy = Objects.requireNonNull(namingStrategy, "namingStrategy");
    }

    @Override
    public String serialize(JoinerQuery<?, ?> joinerQuery) {
        query.setLength(0);
        constants.clear();

        serializeJoinerQuery(joinerQuery);

        return query.toString();
    }

    @Override
    public List<Object> getConstants() {
        return constants;
    }

    // NOTE: subquery serialization reuses the same SQL serializer
    private String serializeSubQuery(JoinerQuery<?, ?> subQuery) {
        JoinerQuery<?, ?> subQueryCopy = subQuery.copy();
        for (JoinDescription join : J.unrollChildrenJoins(subQueryCopy.getJoins())) {
            join.fetch(false);
        }
        serializeJoinerQuery(subQueryCopy);
        return query.toString();
    }

    @Override
    void appendCount(JoinerQuery<?, ?> joinerQuery) {
        query.append("count(1) ");
    }

    protected void appendProjection(JoinerQuery<?, ?> joinerQuery) {
        Expression<?> projection = joinerQuery.getReturnProjection();

        if (projection instanceof FactoryExpressionBase<?> p) {
            // Explicit multi-column / DTO projection: leave as-is
            List<Expression<?>> args = p.getArgs();
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    query.append(", ");
                }
                query.append(serializeExpression(args.get(i)));
            }
        } else if (projection instanceof EntityPath<?> entityPath) {
            // Entity alias -> expand to all mapped columns
            appendEntityProjection(entityPath);
        } else {
            // Scalar expression (path, function, etc.)
            query.append(serializeExpression(projection));
        }
    }


    protected void appendFromCollection(CollectionJoinerQuery<?, ?> joinerQuery) {
        CollectionExpressionBase<?, ?> fromCollection = (CollectionExpressionBase<?, ?>) joinerQuery.getFromCollection();
        query.append(namingStrategy.getTableName(fromCollection.getElementType()))
                .append(" ")
                .append(joinerQuery.getReturnProjection());
    }

    protected void appendFrom(JoinerQuery<?, ?> joinerQuery) {
        Class<?> entityType = joinerQuery.getFrom().getType();
        String alias = joinerQuery.getFrom().getMetadata().getName();
        query.append(namingStrategy.getTableName(entityType))
                .append(" ")
                .append(alias);
    }

    @Override
    protected void appendJoin(JoinerQuery<?, ?> joinerQuery, JoinDescription join) {
        // 1) JOIN keyword
        switch (join.getJoinType()) {
            case LEFTJOIN -> query.append(" left join ");
            case INNERJOIN -> query.append(" inner join ");
            case RIGHTJOIN -> query.append(" right join ");
            default -> query.append(" join ");
        }

        // 2) Target table and alias
        Class<?> targetEntityType = resolveTargetEntityType(join);
        if (targetEntityType == null) {
            throw new JoinerException("Cannot determine target entity type for join: " + join);
        }

        String targetTableName = namingStrategy.getTableName(targetEntityType);
        String rightAlias = resolveAliasName(join.getAlias(), targetTableName);

        query.append(targetTableName).append(" ").append(rightAlias);

        // 3) ON clause: either explicit join.getOn() or implicit from JPA mapping
        String onClause;
        if (join.getOn() != null) {
            onClause = serializeExpression(join.getOn());
        } else {
            onClause = buildImplicitOnClause(join, rightAlias, targetEntityType);
        }

        if (onClause != null && !onClause.isEmpty()) {
            query.append(" on ").append(onClause);
        }
    }


    protected void appendWhere(JoinerQuery<?, ?> joinerQuery) {
        if (joinerQuery.getWhere() != null) {
            Expression<?> where = joinerQuery.getWhere().accept(new JPACollectionAnyVisitor(), new Context());
            query.append(" where ").append(serializeExpression(where));
        }
    }

    protected void appendHaving(JoinerQuery<?, ?> joinerQuery) {
        Predicate having = joinerQuery.getHaving();
        if (having != null) {
            query.append(" having ").append(serializeExpression(having));
        }
    }

    private String serializeExpression(Expression<?> expression) {
        return serializeExpression(expression, null);
    }

    @Override
    protected String serializeExpression(Expression<?> expression, String parentOpOperator) {
        return serializeExpression(expression, parentOpOperator, null);
    }

    private String serializeExpression(Expression<?> expression, String parentOpOperator, Path<?> targetPath) {
        if (expression instanceof com.querydsl.core.types.Constant<?> constantExpr) {
            Object constant = convertConstant(constantExpr.getConstant(), targetPath);

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

        if (expression instanceof Path<?> path) {
            // delegate to naming strategy
            return namingStrategy.toSql(path);
        }

        if (expression instanceof JoinerQuery<?, ?> subQuery) {
            return "(" + new JoinerSQLSerializer(namingStrategy, constants).serializeSubQuery(subQuery) + ")";
        }

        if (expression instanceof Operation<?> operation) {
            List<Expression<?>> args = operation.getArgs();
            String operator = operation.getOperator().toString();

            if (args.size() == 3) {
                String left   = serializeExpression(args.get(0), operator);
                String middle = serializeExpression(args.get(1), operator);
                String right  = serializeExpression(args.get(2), operator);
                return switch (operator) {
                    case "SUBSTR_2ARGS" -> "substring(" + left + ", " + middle + ", " + right + ")";
                    case "BETWEEN"      -> left + " between " + middle + " and " + right;
                    default -> throw new JoinerException("Unsupported operator: " + operator);
                };
            } else if (args.size() == 2) {
                Path<?> pathContext = args.get(0) instanceof Path<?> p ? p : null;
                String left  = serializeExpression(args.get(0), operator);
                String right = serializeExpression(args.get(1), operator, pathContext);

                boolean isConditional         = operation.getOperator() == Ops.AND || operation.getOperator() == Ops.OR;
                boolean isConstantConditional = "AND".equals(parentOpOperator) || "OR".equals(parentOpOperator);
                boolean addParentheses        = isConditional && isConstantConditional;

                String result = serializeOperation(operator, left, right);
                return addParentheses ? "(" + result + ")" : result;
            } else if (args.size() == 1) {
                String arg = serializeExpression(args.get(0));

                return switch (operator) {
                    case "COL_SIZE"        -> throw new IllegalStateException("COL_SIZE op is not supported");
                    case "COL_IS_EMPTY"    -> throw new IllegalStateException("COL_IS_EMPTY op is not supported");
                    case "STRING_IS_EMPTY" -> "length(" + arg + ") = 0";
                    case "STRING_LENGTH"   -> "length(" + arg + ")";
                    case "COALESCE"        -> "coalesce" + arg;
                    case "NOT"             -> "not " + arg;
                    case "IS_NULL"         -> arg + " is null";
                    case "IS_NOT_NULL"     -> arg + " is not null";
                    case "AVG_AGG"         -> "avg(" + arg + ")";
                    case "COUNT_AGG"       -> "count(" + arg + ")";
                    case "COUNT_DISTINCT_AGG" -> "count(distinct " + arg + ")";
                    case "MAX_AGG"         -> "max(" + arg + ")";
                    case "MIN_AGG"         -> "min(" + arg + ")";
                    case "SUM_AGG"         -> "sum(" + arg + ")";
                    case "DISTINCT"        -> "distinct " + arg;
                    case "ALL"             -> "all " + arg;
                    case "ANY"             -> "any " + arg;
                    case "SOME"            -> "some " + arg;
                    case "EXISTS"          -> "exists " + arg;
                    case "NOT_EXISTS"      -> "not exists " + arg;
                    default                -> operator.toLowerCase() + "(" + arg + ")";
                };
            } else {
                throw new JoinerException("Unsupported operator: " + operator);
            }
        }

        return expression.toString();
    }

    private Object convertConstant(Object constant, Path<?> targetPath) {
        if (constant instanceof Enum<?> enumValue) {
            if (targetPath != null) {
                EnumType enumType = resolveEnumType(targetPath);
                if (enumType == EnumType.ORDINAL) {
                    return enumValue.ordinal();
                }}
            return enumValue.name();
        }
        return constant;
    }

    private EnumType resolveEnumType(Path<?> path) {
        PathMetadata metadata = path.getMetadata();
        Path<?> parent = metadata.getParent();
        if (parent == null) return EnumType.STRING;

        Class<?> entityType = parent.getType();
        String fieldName = metadata.getName();

        Field field = findField(entityType, fieldName);
        if (field != null) {
            Enumerated enumAnn = field.getAnnotation(Enumerated.class);
            if (enumAnn != null) {
                return enumAnn.value();
            }
        }
        return EnumType.STRING; // default
    }


    private static String serializeOperation(String operator, String left, String right) {
        return switch (operator) {
            case "EQ"  -> left + " = " + right;
            case "NE"  -> left + " <> " + right;
            case "GT"  -> left + " > " + right;
            case "GOE" -> left + " >= " + right;
            case "LT"  -> left + " < " + right;
            case "LOE" -> left + " <= " + right;
            case "LIKE_IC", "ENDS_WITH_IC", "STARTS_WITH_IC", "STRING_CONTAINS_IC" ->
                    "lower(" + left + ") like " + right;
            case "STARTS_WITH", "STRING_CONTAINS", "LIKE", "ENDS_WITH" ->
                    left + " like " + right;
            case "LIKE_ESCAPE", "LIKE_ESCAPE_IC" ->
                    left + " like " + right + " escape '!'";
            case "IN"      -> left + " in " + right;
            case "NOT_IN"  -> left + " not in " + right;
            case "CONCAT"  -> "concat(" + left + ", " + right + ")";
            case "SUBSTR_1ARG" -> "substring(" + left + ", " + right + ")";
            case "LOCATE"  -> "locate(" + left + ", " + right + ")";
            case "MOD"     -> "mod(" + left + ", " + right + ")";
            case "COALESCE"-> "coalesce(" + left + ", " + right + ")";
            case "NULLIF"  -> "nullif(" + left + ", " + right + ")";
            case "CAST"    -> "cast(" + left + " as " + right + ")";
            case "TREAT"   -> "/* treat() is JPQL-only */ " + left;
            case "LIST"    -> "(" + left + ", " + right + ")";
            default -> left + " " + operator.toLowerCase().replaceAll("_", " ") + " " + right;
        };
    }

    private static String lowered(Object constant) {
        return constant != null ? constant.toString().toLowerCase() : null;
    }

    private Class<?> resolveTargetEntityType(JoinDescription join) {
        if (join.isCollectionPath() && join.getCollectionPath() != null) {
            // For collection association: element type is the target entity
            CollectionPathBase<?, ?, ?> collectionPath = join.getCollectionPath();
            return collectionPath.getElementType();  // QueryDSL's CollectionPathBase has getElementType()
        }

        if (join.getSingularPath() != null) {
            // For singular association (ManyToOne, OneToOne, etc.)
            return join.getSingularPath().getType();
        }

        if (join.getPath() instanceof EntityPath<?> entityPath) {
            return entityPath.getType();
        } if (join.getPath() instanceof CollectionPathBase<?, ?, ?> entityPath) {
            return entityPath.getElementType();
        }

        return null;
    }

    private String resolveAliasName(EntityPath<?> aliasPath, String fallbackBase) {
        if (aliasPath != null) {
            return aliasPath.getMetadata().getName();
        }
        // if for some reason alias is null, fallback to table name
        return fallbackBase;
    }


    private String buildImplicitOnClause(JoinDescription join,
                                         String rightAlias,
                                         Class<?> rightEntityType) {

        // Determine association path (u.address, u.orders, etc.)
        Expression<?> associationExpr =
                join.isCollectionPath() ? join.getCollectionPath() : join.getSingularPath();
        if (associationExpr == null) associationExpr = join.getPath();

        if (!(associationExpr instanceof Path<?> assocPath)) {
            // No association path -> nothing to infer automatically
            return "";
        }

        PathMetadata metadata = assocPath.getMetadata();
        Path<?> parentPath = metadata.getParent();
        if (parentPath == null) {
            throw new JoinerException("Association path has no parent: " + assocPath);
        }

        String leftAlias = parentPath.getMetadata().getName();
        Class<?> leftEntityType = parentPath.getType();
        String associationName = metadata.getName(); // field name on left side

        Field associationField = findField(leftEntityType, associationName);
        if (associationField == null) {
            throw new JoinerException("Cannot find field '" + associationName +
                    "' on entity " + leftEntityType.getName());
        }

        // Try @JoinColumns first
        JoinColumns joinColumnsAnn = associationField.getAnnotation(JoinColumns.class);
        JoinColumn[] joinColumns = null;

        if (joinColumnsAnn != null && joinColumnsAnn.value().length > 0) {
            joinColumns = joinColumnsAnn.value();
        } else {
            JoinColumn jc = associationField.getAnnotation(JoinColumn.class);
            if (jc != null) {
                joinColumns = new JoinColumn[]{ jc };
            }
        }

        List<String> predicates = new ArrayList<>();

        if (joinColumns != null && joinColumns.length > 0) {
            for (JoinColumn jc : joinColumns) {
                String leftColumn = jc.name(); // FK column on left side
                if (leftColumn == null || leftColumn.isEmpty()) {
                    // fallback: derived from field name
                    leftColumn = getColumnName(leftEntityType, associationName);
                }

                String referencedColumn = jc.referencedColumnName();
                if (referencedColumn == null || referencedColumn.isEmpty()) {
                    referencedColumn = getIdColumnName(rightEntityType);
                }

                predicates.add(leftAlias + "." + leftColumn + " = " + rightAlias + "." + referencedColumn);
            }
        } else {
            // No @JoinColumn – likely mappedBy side or ManyToMany / OneToMany without explicit join columns.
            // Fallback heuristic: join by primary keys (not always correct, but better than nothing).
            String leftIdColumn = getIdColumnName(leftEntityType);
            String rightIdColumn = getIdColumnName(rightEntityType);
            predicates.add(leftAlias + "." + leftIdColumn + " = " + rightAlias + "." + rightIdColumn);
        }

        return String.join(" and ", predicates);
    }

    private Field findField(Class<?> type, String name) {
        Class<?> current = type;
        while (current != null && current != Object.class) {
            try {
                Field f = current.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
            current = current.getSuperclass();
        }
        return null;
    }

    private String getColumnName(Class<?> entityType, String fieldName) {
        Field field = findField(entityType, fieldName);
        if (field == null) return fieldName;

        Column col = field.getAnnotation(Column.class);
        if (col != null && !col.name().isEmpty()) {
            return col.name();
        }

        // fallback: just use the field name
        return fieldName;
    }

    private String getIdColumnName(Class<?> entityType) {
        // 1) Look for @Id field with @Column(name=...)
        Class<?> current = entityType;
        while (current != null && current != Object.class) {
            for (Field f : current.getDeclaredFields()) {
                if (f.isAnnotationPresent(Id.class)) {
                    Column col = f.getAnnotation(Column.class);
                    if (col != null && !col.name().isEmpty()) {
                        return col.name();
                    }
                    return f.getName(); // fallback to field name
                }
            }
            current = current.getSuperclass();
        }
        // 2) Last-resort default
        return "id";
    }

    private void appendEntityProjection(EntityPath<?> entityPath) {
        Class<?> entityType = entityPath.getType();
        String alias = entityPath.getMetadata().getName();

        List<String> columns = getPersistentColumns(entityType, alias);
        if (columns.isEmpty()) {
            // last resort, but should not normally happen
            query.append(alias).append(".*");
            return;
        }

        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                query.append(", ");
            }
            query.append(columns.get(i));
        }
    }

    private List<String> getPersistentColumns(Class<?> entityType, String alias) {
        List<String> result = new ArrayList<>();
        Class<?> current = entityType;

        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                if (shouldSkipField(field)) continue;

                String columnName = resolveColumnName(field);
                String selectFragment = alias + "." + columnName;
                result.add(selectFragment);
            }

            DiscriminatorColumn discriminator = current.getAnnotation(DiscriminatorColumn.class);
            if (discriminator != null) {
                result.add(alias + "." + discriminator.name());
            }

            current = current.getSuperclass();
        }

        return result;
    }

    private boolean shouldSkipField(Field field) {
        int mod = field.getModifiers();

        // 1) JPA ignores static fields (serialVersionUID, constants, helpers, etc.)
        if (Modifier.isStatic(mod)) {
            return true;
        }

        // 2) Synthetic fields generated by compiler
        if (field.isSynthetic()) {
            return true;
        }

        // 3) Explicit JPA @Transient or Java transient keyword
        if (field.isAnnotationPresent(jakarta.persistence.Transient.class)
                || Modifier.isTransient(mod)) {
            return true;
        }

        // 4) Association mappings are not scalar columns
        if (field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(ElementCollection.class)) {
            return true; // skip
        }

        return false;
    }

    private static final Logger log = LoggerFactory.getLogger(JoinerSQLSerializer.class);

    private String resolveColumnName(Field field) {
        jakarta.persistence.Column col = field.getAnnotation(jakarta.persistence.Column.class);
        if (col != null && !col.name().isEmpty()) {
            return col.name();
        }
        if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
            // @Id without @Column(name=...) – use field name
            return field.getName();
        }
        jakarta.persistence.JoinColumn jcol = field.getAnnotation(jakarta.persistence.JoinColumn.class);
        if (jcol != null && !jcol.name().isEmpty()) {
            return jcol.name();
        }
        return field.getName();
    }

}
