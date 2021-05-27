package cz.encircled.joiner.util;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.NormalUser;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.SuperUser;
import cz.encircled.joiner.model.User;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Vlad on 01-Nov-16.
 */
public class ReflectionUtilsTest extends AbstractTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testInstantiateQ() {
        EntityPath user = ReflectionUtils.instantiate(QSuperUser.class, "testAlias");
        assertEquals(QSuperUser.class, user.getClass());
        assertEquals("testAlias", user.toString());
    }

    @Test
    public void testInstantiateQException() {
        assertThrows(JoinerException.class, () -> ReflectionUtils.instantiate(QSuperUser.class, null));
    }

    @Test
    public void testFindFieldException() {
        assertNull(ReflectionUtils.findField(QSuperUser.class, "notExists"));
    }

    @Test
    public void testGetField() {
        assertSame(entityManager, ReflectionUtils.getField("entityManager", this));
        assertSame(entityManager, ReflectionUtils.getField(ReflectionUtils.findField(this.getClass(), "entityManager"), this));
    }

    @Test
    public void testSetField() {
        EntityManager original = entityManager;
        try {
            ReflectionUtils.setField(ReflectionUtils.findField(this.getClass(), "entityManager"), this, null);
            assertNull(entityManager);
        } finally {
            entityManager = original;
        }
    }

    @Test
    public void testGetSubclasses() {
        Set<Class> subclasses = ReflectionUtils.getSubclasses(User.class, entityManager);
        assertEquals(2, subclasses.size());
        assertTrue(subclasses.contains(SuperUser.class));
        assertTrue(subclasses.contains(NormalUser.class));
    }

}
