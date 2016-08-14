package cz.encircled.joiner.test.repository.impl;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.Address;
import cz.encircled.joiner.test.model.QAddress;
import cz.encircled.joiner.test.repository.AddressRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 26.01.2016.
 */
@Repository
public class AddressRepositoryImpl extends SpringJoinerRepository<Address> implements AddressRepository {

    @Override
    protected EntityPath<Address> getRootEntityPath() {
        return QAddress.address;
    }

}
