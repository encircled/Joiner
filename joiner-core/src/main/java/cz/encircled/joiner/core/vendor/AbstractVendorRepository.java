package cz.encircled.joiner.core.vendor;

import java.util.List;

import com.querydsl.core.types.CollectionExpression;
import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.jpa.impl.JPAQuery;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.join.JoinDescription;

/**
 * Common parent for implementations of vendor-specific repositories
 *
 * @author Kisel on 28.01.2016.
 */
public abstract class AbstractVendorRepository implements JoinerVendorRepository {

    @Override
    @SuppressWarnings("unchecked")
    public void addJoin(JPAQuery query, JoinDescription joinDescription) {
        Path<Object> alias = (Path<Object>) joinDescription.getAlias();

        switch (joinDescription.getJoinType()) {
            case LEFTJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.leftJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.leftJoin(singlePath, alias);
                }
                break;
            case INNERJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.innerJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.innerJoin(singlePath, alias);
                }
                break;
            case RIGHTJOIN:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.rightJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.rightJoin(singlePath, alias);
                }
                break;
            default:
                throw new JoinerException("Join type " + joinDescription.getJoinType() + " is not supported!");
        }

        if (joinDescription.isFetch()) {
            query.fetchJoin();
        }

        if (joinDescription.getOn() != null) {
            query.on(joinDescription.getOn());
        }
    }

    @Override
    public <T> List<T> getResultList(JPAQuery query, Expression<T> projection) {
        return query.fetch();
    }
}
