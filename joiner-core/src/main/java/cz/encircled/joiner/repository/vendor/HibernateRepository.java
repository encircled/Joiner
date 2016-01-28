package cz.encircled.joiner.repository.vendor;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.HQLTemplates;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.CollectionExpression;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Path;
import cz.encircled.joiner.query.JoinDescription;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository implements JoinerVendorRepository {

    public JPAQuery createQuery(EntityManager entityManager) {
        return new JPAQuery(entityManager, HQLTemplates.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public void addJoin(JPAQuery query, JoinDescription joinDescription) {
        Path<Object> alias = (Path<Object>) joinDescription.getAlias();

        switch (joinDescription.getJoinType()) {
            case LEFT:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.leftJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.leftJoin(singlePath, alias);
                }
                break;
            case INNER:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.innerJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.innerJoin(singlePath, alias);
                }
                break;
            case RIGHT:
                if (joinDescription.isCollectionPath()) {
                    CollectionExpression<?, Object> collectionPath = (CollectionExpression<?, Object>) joinDescription.getCollectionPath();
                    query.rightJoin(collectionPath, alias);
                } else {
                    EntityPath<Object> singlePath = (EntityPath<Object>) joinDescription.getSinglePath();
                    query.rightJoin(singlePath, alias);
                }
                break;
        }

        if (joinDescription.getOn() != null) {
            query.on(joinDescription.getOn());
        }
    }

    public void addFetch(JPAQuery query, JoinDescription joinDescription) {
        query.fetch();
    }

}
