package cz.encircled.joiner.test.repository

import com.mysema.query.types.EntityPath
import cz.encircled.joiner.spring.SpringJoinerRepository
import cz.encircled.joiner.test.model.User
import org.springframework.stereotype.Repository

/**
 * @author Kisel on 21.01.2016.
 */
@Repository
class UserRepositoryImpl : SpringJoinerRepository<User>(), UserRepository {

    protected override val rootEntityPath: EntityPath<User>?
        get() = QUser.user1

}
