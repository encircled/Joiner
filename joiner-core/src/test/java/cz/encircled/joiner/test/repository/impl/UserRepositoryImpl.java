package cz.encircled.joiner.test.repository.impl;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import cz.encircled.joiner.test.repository.UserRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 21.01.2016.
 */
@Repository
public class UserRepositoryImpl extends SpringJoinerRepository<User> implements UserRepository {

    @Override
    protected EntityPath<User> getRootEntityPath() {
        return QUser.user1;
    }

}
