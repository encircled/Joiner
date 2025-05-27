package cz.encircled.joiner.core.vendor;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.JPQLQuery;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinDescription;
import jakarta.persistence.Query;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Common parent for implementations of vendor-specific repositories
 *
 * @author Kisel on 28.01.2016.
 */
public abstract class AbstractVendorRepository implements JoinerVendorRepository {

    protected void setQueryParams(JoinerJPQLSerializer serializer, Query query, JoinerQuery<?, ?> request) {
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

        for (Map.Entry<String, List<Object>> hintValues : request.getHints().entrySet()) {
            for (Object hintVal : hintValues.getValue()) {
                query.setHint(hintValues.getKey(), hintVal);
            }
        }
    }

}
