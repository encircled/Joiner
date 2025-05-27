package cz.encircled.joiner.core.vendor;

import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinerJpaQuery implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(JoinerJpaQuery.class);

    final AutoCloseable session;
    public final Query jpaQuery;
    public final String queryString;

    public JoinerJpaQuery(Query jpaQuery, String queryString, AutoCloseable session) {
        this.session = session;
        this.jpaQuery = jpaQuery;
        this.queryString = queryString;
    }

    @Override
    public void close() {
        if (session != null) {
            try {
                session.close();
            } catch (Exception e) {
                log.warn("Failed to close session for a JPA query", e);
            }
        }
    }
}
