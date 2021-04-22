package cz.encircled.joiner.repository;

import cz.encircled.joiner.model.User;
import cz.encircled.joiner.spring.SpringJoinerRepositoryImpl;
import org.springframework.stereotype.Repository;

/**
 * @author Kisel on 21.01.2016.
 */
@Repository
public class UserRepositoryImpl extends SpringJoinerRepositoryImpl<User> implements UserRepository {

}
