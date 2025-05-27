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

/**
 * Common parent for implementations of vendor-specific repositories
 *
 * @author Kisel on 28.01.2016.
 */
public abstract class AbstractVendorRepository implements JoinerVendorRepository {

    @Override
    @SuppressWarnings("unchecked")
    public void addJoin(JPQLQuery<?> query, JoinDescription joinDescription) {
        Path<Object> alias = (Path<Object>) joinDescription.getAlias();

        switch (joinDescription.getJoinType()) {
            case LEFTJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.leftJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSingularPath();
                    query.leftJoin(singlePath, alias);
                }
                break;
            case INNERJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.innerJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSingularPath();
                    query.innerJoin(singlePath, alias);
                }
                break;
            case RIGHTJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.rightJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSingularPath();
                    query.rightJoin(singlePath, alias);
                }
                break;
            default:
                throw new JoinerException("Join type " + joinDescription.getJoinType() + " is not supported!");
        }

        if (joinDescription.getOn() != null) {
            query.on(joinDescription.getOn());
        }
    }

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
    }

}
