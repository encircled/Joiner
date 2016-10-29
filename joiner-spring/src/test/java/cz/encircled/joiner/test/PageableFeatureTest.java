package cz.encircled.joiner.test;

import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.spring.PageableFeature;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.data.domain.PageRequest;

/**
 * @author Kisel on 29.10.2016.
 */
public class PageableFeatureTest {

    @Test
    public void testLimitAndOffset() {
        JoinerQuery<User, User> request = Q.from(QUser.user1);
        new PageableFeature(new PageRequest(2, 10)).before(request);

        Assert.assertEquals(Long.valueOf(10L), request.getLimit());
        Assert.assertEquals(Long.valueOf(20L), request.getOffset());
    }

}
