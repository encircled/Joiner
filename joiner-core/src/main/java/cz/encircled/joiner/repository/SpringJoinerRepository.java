package cz.encircled.joiner.repository;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author Kisel on 26.01.2016.
 */
public abstract class SpringJoinerRepository<T> extends JoinerRepository<T> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

}
