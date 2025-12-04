package cz.encircled.joiner.core.serializer;

import com.querydsl.core.types.Path;

public interface SqlNamingStrategy {

    /**
     * @param entityType JPA entity class
     * @return physical table name to be used in SQL
     */
    String getTableName(Class<?> entityType);

    /**
     * @param path QueryDSL path (can be root alias or nested path)
     * @return SQL fragment for this path, e.g. "u.id" or "o.customer_id"
     */
    String toSql(Path<?> path);
}
