package cz.encircled.joiner;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.HQLTemplates;
import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.ListPath;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.query.JoinDescription;

/**
 * @author Kisel on 21.01.2016.
 */
public class HibernateRepository implements JoinerRepository {

    public JPAQuery createQuery(EntityManager entityManager) {
        return new JPAQuery(entityManager, HQLTemplates.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    public void addJoin(JPAQuery query, JoinDescription joinDescription) {
        ListPath<Object, ?> listPath = (ListPath<Object, ?>) joinDescription.getListPath();
        Path<Object> alias = (Path<Object>) joinDescription.getAlias();

        switch (joinDescription.getJoinType()) {
            case LEFT:
                query.leftJoin(listPath, alias);
                break;
            case INNER:
                query.innerJoin(listPath, alias);
                break;
            case RIGHT:
                throw new JoinerException("Right join is not supported!");
        }
    }

    public void addFetch(JPAQuery query) {
        query.fetch();
    }

}
