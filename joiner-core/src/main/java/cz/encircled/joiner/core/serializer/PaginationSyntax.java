package cz.encircled.joiner.core.serializer;

/**
 * Represents the SQL pagination syntax used by different databases.
 *
 * <ul>
 *     <li>{@link #LIMIT_OFFSET} — {@code LIMIT n OFFSET m} (MySQL, PostgreSQL, MariaDB, SQLite, H2)</li>
 *     <li>{@link #OFFSET_FETCH} — {@code OFFSET m ROWS FETCH FIRST n ROWS ONLY} (SQL:2008 standard, Oracle 12c+, SQL Server 2012+)</li>
 * </ul>
 */
public enum PaginationSyntax {

    /**
     * {@code LIMIT n OFFSET m} — MySQL, PostgreSQL, MariaDB, SQLite, H2
     */
    LIMIT_OFFSET,

    /**
     * {@code OFFSET m ROWS FETCH FIRST n ROWS ONLY} — SQL:2008 standard, Oracle 12c+, SQL Server 2012+
     */
    OFFSET_FETCH;

    /**
     * Detect the appropriate pagination syntax from a Hibernate dialect class name.
     *
     * @param dialectClassName fully qualified or simple class name of the Hibernate dialect
     * @return the matching pagination syntax, defaults to {@link #OFFSET_FETCH}
     */
    public static PaginationSyntax fromDialectName(String dialectClassName) {
        if (dialectClassName == null) {
            return OFFSET_FETCH;
        }
        String lower = dialectClassName.toLowerCase();
        if (lower.contains("mysql") || lower.contains("mariadb") || lower.contains("postgres")
                || lower.contains("sqlite") || lower.contains("h2") || lower.contains("cockroach")) {
            return LIMIT_OFFSET;
        }
        return OFFSET_FETCH;
    }

}

