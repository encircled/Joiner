package cz.encircled.joiner.test.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.Group;
import cz.encircled.joiner.test.model.QGroup;
import cz.encircled.joiner.test.repository.GroupRepository;
import cz.encircled.joiner.test.repository.RepositoryParent;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 25.01.2016.
 */
@Repository
public class GroupRepositoryImpl extends RepositoryParent<Group> implements GroupRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected EntityPath<Group> getRootEntityPath() {
        return QGroup.group;
    }

}
