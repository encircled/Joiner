package cz.encircled.joiner.test.repository.impl;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.repository.AddressRepository;
import cz.encircled.joiner.test.repository.RepositoryParent;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 26.01.2016.
 */
@Repository
public class AddressRepositoryImpl extends RepositoryParent<Address> implements AddressRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    @Override
    protected EntityPath<Address> getRootEntityPath() {
        return QAddress.address;
    }

}
