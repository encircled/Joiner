package cz.encircled.joiner.repository.vendor;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.CollectionExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinDescription;

/**
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

        if (joinDescription.getOn() != null) {
            query.on(joinDescription.getOn());
        }
    }

}
