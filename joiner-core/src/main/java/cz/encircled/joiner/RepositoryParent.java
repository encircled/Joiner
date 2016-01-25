package cz.encircled.joiner;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import org.springframework.util.Assert;

/**
 * @author Kisel on 21.01.2016.
 */
public abstract class RepositoryParent<T> implements QRepository<T> {

    private EntityManager entityManager;

    private EntityPath<T> rootPath;

    private JoinerRepository joinerRepository;

    public List<T> find(Q<T> request) {
        return find(request, rootPath);
    }

    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        JPAQuery query = joinerRepository.createQuery(entityManager);
        query.from(rootPath);

        for (JoinDescription join : request.getJoins()) {
            if (join.getAlias() == null) {
                join.alias(JoinerUtil.getDefaultAlias(join.getListPath()));
            }

            joinerRepository.addJoin(query, join);
            if (join.isFetch()) {
                joinerRepository.addFetch(query);
            }
        }

        return query.list(projection);
    }

    public T findOne(Q<T> request) {
        return null;
    }

    public <P> P findOne(Q<T> request, Expression<P> projection) {
        return null;
    }


    @PostConstruct
    private void init() {
        entityManager = getEntityManager();
        rootPath = getRootEntityPath();
        joinerRepository = new HibernateRepository();

        Assert.notNull(entityManager);
        Assert.notNull(rootPath);
    }

    protected abstract EntityManager getEntityManager();

    protected abstract EntityPath<T> getRootEntityPath();

}
