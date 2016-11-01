package cz.encircled.joiner.test;

import com.mysema.query.types.EntityPath;
import cz.encircled.joiner.test.core.AbstractTest;
import cz.encircled.joiner.test.model.NormalUser;
import cz.encircled.joiner.test.model.QSuperUser;
import cz.encircled.joiner.test.model.SuperUser;
import cz.encircled.joiner.test.model.User;
import cz.encircled.joiner.util.ReflectionUtils;
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

    @Test
    public void testGetSubclasses() {
        Set<Class> subclasses = ReflectionUtils.getSubclasses(User.class, entityManager);
        Assert.assertEquals(2, subclasses.size());
        Assert.assertTrue(subclasses.contains(SuperUser.class));
        Assert.assertTrue(subclasses.contains(NormalUser.class));
    }

}
