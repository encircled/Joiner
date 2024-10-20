package cz.encircled.joiner.util;

import cz.encircled.joiner.model.*;
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

    @Test
    public void testChildPath() {
        assertEquals(
                new QUser("user"),
                JoinerUtils.getLastElementPath(QStatus.status.user)
        );
        assertEquals(
                QUser.user1,
                JoinerUtils.getLastElementPath(QUser.user1)
        );
    }

}
