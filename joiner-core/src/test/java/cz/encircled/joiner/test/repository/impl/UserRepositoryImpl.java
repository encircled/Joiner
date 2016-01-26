package cz.encircled.joiner.test.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import cz.encircled.joiner.test.repository.RepositoryParent;
import cz.encircled.joiner.test.repository.UserRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 21.01.2016.
 */
@Repository
public class UserRepositoryImpl extends RepositoryParent<User> implements UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected EntityPath<User> getRootEntityPath() {
        return QUser.user;
    }

}
