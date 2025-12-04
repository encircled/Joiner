package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.core.serializer.JoinerSerializer;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Vendor-specific repositories
 *
 * @author Kisel on 28.01.2016.
 */
public abstract class VendorRepository {

    public void addFetch(JoinDescription joinDescription, Collection<JoinDescription> joins, EntityPath<?> rootPath, JoinerQuery<?, ?> request) {}

    public abstract JoinerJpaQuery createQuery(JoinerQuery<?, ?> request, JoinerProperties joinerProperties, EntityManager entityManager);

    public abstract <T> List<T> fetchResult(JoinerQuery<?, T> request, Query jpaQuery);

    public abstract <T> Stream<T> streamResult(JoinerQuery<?, T> request, Query jpaQuery);

    protected void setQueryParams(JoinerSerializer serializer, Query query, JoinerQuery<?, ?> request, JoinerProperties joinerProperties) {
        List<Object> constants = serializer.getConstants();
        for (int i = 0; i < constants.size(); i++) {
            Object val = constants.get(i);
            if (val instanceof Collection<?>) {
                query.setParameter(i + 1, val);
            } else {
                query.setParameter(i + 1, val);
            }
        }

        if (request.getLimit() != null) {
            query.setMaxResults(request.getLimit());
        }
        if (request.getOffset() != null) {
            query.setFirstResult(request.getOffset());
        }

        if (request.getFlushMode() != null) {
            query.setFlushMode(request.getFlushMode());
        }

        for (Map.Entry<String, List<Object>> hintValues : request.getHints().entrySet()) {
            for (Object hintVal : hintValues.getValue()) {
                query.setHint(hintValues.getKey(), hintVal);
            }
        }
        for (Map.Entry<String, List<Object>> entry : joinerProperties.defaultHints.entrySet()) {
            for (Object value : entry.getValue()) {
                query.setHint(entry.getKey(), value);
            }
        }
    }

}
