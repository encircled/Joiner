package cz.encircled.joiner.util;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QPassword;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.QUser;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class JoinerUtilsTest {

    @Test
    public void testCollectPredicatePaths() {
        Assert.assertEquals(
                Arrays.asList(QSuperUser.superUser.id, QPassword.password.name),
                JoinerUtils.collectPredicatePaths(QSuperUser.superUser.id.eq(1L).or(QPassword.password.name.eq("")))
        );
    }

    @Test
    public void testGetDefaultPath() {
        Assert.assertEquals(
                QSuperUser.superUser,
                JoinerUtils.getDefaultPath(QSuperUser.class)
        );
        Assert.assertEquals(
                QUser.user1,
                JoinerUtils.getDefaultPath(QUser.class)
        );
        Assert.assertEquals(
                QUser.user1,
                JoinerUtils.getDefaultPath(QGroup.group.users)
        );
    }

}
