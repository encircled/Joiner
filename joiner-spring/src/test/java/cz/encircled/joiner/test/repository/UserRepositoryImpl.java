package cz.encircled.joiner.test.repository;

import cz.encircled.joiner.spring.SpringJoinerRepositoryImpl;
import cz.encircled.joiner.test.model.User;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 21.01.2016.
 */
@Repository
public class UserRepositoryImpl extends SpringJoinerRepositoryImpl<User> implements UserRepository {

}
