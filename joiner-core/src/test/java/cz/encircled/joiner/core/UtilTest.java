package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QGroup;
import cz.encircled.joiner.model.QNormalUser;
import cz.encircled.joiner.model.QPhone;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.join.J;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Vlad on 06-Nov-16.
 */
public class UtilTest {

    @Test
    public void testJPath() {
        assertEquals("user1_on_group1", J.path(QGroup.group, QUser.user1).toString());
    }

    @Test
    public void testJPathViaParent() {
        assertEquals("user1_on_group1", J.path(QGroup.group.users).toString());
        assertSame(QUser.class, J.path(QGroup.group.users).getClass());
    }

    @Test
    public void testNestedJPath() {
        assertEquals("phone_on_user1_on_group1", J.path(QGroup.group, QUser.user1, QPhone.phone).toString());
    }

    @Test
    public void testJoinsEqualsByName() {
        assertEquals(J.left(QUser.user1), J.left(new QNormalUser("user1")));
    }

}
