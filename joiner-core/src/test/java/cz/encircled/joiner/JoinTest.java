package cz.encircled.joiner;

import java.util.Collections;
import java.util.List;

import javax.persistence.Persistence;

import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.model.User;
import cz.encircled.joiner.repository.UserRepository;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Kisel on 21.01.2016.
 */
public class JoinTest extends AbstractTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void joinTest() {
        prepareData();

        List<User> users = userRepository.find(Q.from(QUser.user));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        JoinDescription e = new JoinDescription(QUser.user.groups).fetch(false);

        users = userRepository.find(Q.from(QUser.user).joins(Collections.singletonList(e)));
        Assert.assertFalse(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));

        e.fetch(true);
        users = userRepository.find(Q.from(QUser.user).joins(Collections.singletonList(e)));
        Assert.assertTrue(Persistence.getPersistenceUtil().isLoaded(users.get(0), "groups"));
    }

}
