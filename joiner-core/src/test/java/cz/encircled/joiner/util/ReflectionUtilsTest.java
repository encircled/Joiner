package cz.encircled.joiner.util;

import com.querydsl.core.types.EntityPath;
import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.NormalUser;
import cz.encircled.joiner.model.QSuperUser;
import cz.encircled.joiner.model.SuperUser;
import cz.encircled.joiner.model.User;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

/**
 * @author Vlad on 01-Nov-16.
 */
public class ReflectionUtilsTest extends AbstractTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testInstantiateQ() {
        EntityPath user = ReflectionUtils.instantiate(QSuperUser.class, "testAlias");
        Assert.assertEquals(QSuperUser.class, user.getClass());
        Assert.assertEquals("testAlias", user.toString());
    }

    @Test(expected = JoinerException.class)
    public void testInstantiateQException() {
        ReflectionUtils.instantiate(QSuperUser.class, null);
    }

    @Test
    public void testFindFieldException() {
        Assert.assertNull(ReflectionUtils.findField(QSuperUser.class, "notExists"));
    }

    @Test
    public void testGetField() {
        Assert.assertSame(entityManager, ReflectionUtils.getField("entityManager", this));
        Assert.assertSame(entityManager, ReflectionUtils.getField(ReflectionUtils.findField(this.getClass(), "entityManager"), this));
    }

    @Test
    public void testSetField() {
        EntityManager original = entityManager;
        try {
            ReflectionUtils.setField(ReflectionUtils.findField(this.getClass(), "entityManager"), this, null);
            Assert.assertNull(entityManager);
        } finally {
            entityManager = original;
        }
    }

    @Test
    public void testGetSubclasses() {
        Set<Class> subclasses = ReflectionUtils.getSubclasses(User.class, entityManager);
        Assert.assertEquals(2, subclasses.size());
        Assert.assertTrue(subclasses.contains(SuperUser.class));
        Assert.assertTrue(subclasses.contains(NormalUser.class));
    }

}
