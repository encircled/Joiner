package cz.encircled.joiner.eclipse;

import com.querydsl.core.types.FactoryExpression;
import cz.encircled.joiner.core.JoinerJPQLSerializer;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.core.vendor.EclipselinkRepository;
import cz.encircled.joiner.core.vendor.JoinerVendorRepository;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.util.ReflectionUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.eclipse.persistence.internal.jpa.QueryImpl;
import org.eclipse.persistence.internal.queries.JoinedAttributeManager;
import org.eclipse.persistence.queries.ObjectLevelReadQuery;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kisel on 28.01.2016.
 */
public class EnchancedEclipselinkRepository extends EclipselinkRepository implements JoinerVendorRepository {

    // TODO test for fixed joinedAttributeManager is missing
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> getResultList(JoinerQuery<?, T> request, JoinerProperties joinerProperties, EntityManager entityManager) {
        JoinerJPQLSerializer serializer = new JoinerJPQLSerializer();
        String queryString = serializer.serialize(request, request.isCount());
        Query jpaQuery = entityManager.createQuery(queryString);

        if (jpaQuery instanceof QueryImpl) {
            QueryImpl casted = (QueryImpl) jpaQuery;
            if (casted.getDatabaseQuery() instanceof ObjectLevelReadQuery) {
                Field f = ReflectionUtils.findField(ObjectLevelReadQuery.class, "joinedAttributeManager");
                f.setAccessible(true);
                JoinedAttributeManager old = (JoinedAttributeManager) ReflectionUtils.getField(f, casted.getDatabaseQuery());
                if (old != null) {
                    FixedJoinerAttributeManager newManager = new FixedJoinerAttributeManager(old.getDescriptor(), old.getBaseExpressionBuilder(),
                            old.getBaseQuery());
                    newManager.copyFrom(old);
                    ReflectionUtils.setField(f, casted.getDatabaseQuery(), newManager);
                }
            }
        }

        if (request.getReturnProjection() instanceof FactoryExpression) {
            FactoryExpression factoryExpression = (FactoryExpression) request.getReturnProjection();

            List<?> results = jpaQuery.getResultList();
            List<Object> rv = new ArrayList<>(results.size());
            for (Object o : results) {
                if (o != null) {
                    if (!o.getClass().isArray()) {
                        o = new Object[]{o};
                    }
                    rv.add(factoryExpression.newInstance((Object[]) o));
                } else {
                    rv.add(null);
                }
            }
            return (List<T>) rv;
        } else {
            return jpaQuery.getResultList();
        }
    }

}
