package cz.encircled.joiner.util;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QPassword;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.QUser;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JoinerUtilsTest {

    @Test
    public void testCollectPredicatePaths() {
        assertEquals(
                Arrays.asList(QSuperUser.superUser.id, QPassword.password.name),
                JoinerUtils.collectPredicatePaths(QSuperUser.superUser.id.eq(1L).or(QPassword.password.name.eq("")))
        );
    }

    @Test
    public void testGetDefaultPath() {
        assertEquals(
                QSuperUser.superUser,
                JoinerUtils.getDefaultPath(QSuperUser.class)
        );
        assertEquals(
                QUser.user1,
                JoinerUtils.getDefaultPath(QUser.class)
        );
        assertEquals(
                QUser.user1,
                JoinerUtils.getDefaultPath(QGroup.group.users)
        );
    }

}
