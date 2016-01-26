package cz.encircled.joiner.repository;

import java.util.List;

import javax.persistence.EntityManager;

import com.mysema.query.jpa.impl.JPAQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.Expression;
import cz.encircled.joiner.query.JoinDescription;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.repository.vendor.HibernateRepository;
import cz.encircled.joiner.repository.vendor.JoinerVendorRepository;
import cz.encircled.joiner.util.JoinerUtil;
import org.springframework.util.Assert;

/**
 * @author Kisel on 26.01.2016.
 */
public class Joiner<T> implements QRepository<T> {

    private EntityManager entityManager;

    private EntityPath<T> rootPath;

    private JoinerVendorRepository joinerVendorRepository;

    public Joiner(EntityManager entityManager, EntityPath<T> rootPath) {
        Assert.notNull(entityManager);
        Assert.notNull(rootPath);

        this.entityManager = entityManager;
        this.rootPath = rootPath;
        joinerVendorRepository = new HibernateRepository(); // TODO
    }

    public List<T> find(Q<T> request) {
        return find(request, rootPath);
    }

    public <P> List<P> find(Q<T> request, Expression<P> projection) {
        JPAQuery query = joinerVendorRepository.createQuery(entityManager);
        query.from(rootPath);

        for (JoinDescription join : request.getJoins()) {
            if (join.getAlias() == null) {
                join.alias(JoinerUtil.getDefaultAlias(join.getListPath()));
            }

            joinerVendorRepository.addJoin(query, join);
            if (join.isFetch()) {
                joinerVendorRepository.addFetch(query);
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

}
