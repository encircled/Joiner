package cz.encircled.joiner.core;

import cz.encircled.joiner.TestDataListener;
import cz.encircled.joiner.config.TestConfig;
import cz.encircled.joiner.model.AbstractEntity;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.Collection;

/**
 * @author Kisel on 11.01.2016.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@TestExecutionListeners(listeners = {TestDataListener.class})
public abstract class AbstractTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    protected Joiner joiner;

    @Autowired
    protected JoinGraphRegistry joinGraphRegistry;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private Environment environment;

    protected void assertHasName(Collection<? extends AbstractEntity> entities, String name) {
        Assert.assertFalse("Found collection must be not empty!", entities.isEmpty());
        for (AbstractEntity entity : entities) {
            assertHasName(entity, name);
        }
    }

    protected void assertHasName(AbstractEntity entity, String name) {
        Assert.assertNotNull(entity);
        Assert.assertEquals(name, entity.getName());
    }

    protected boolean isEclipse() {
        return hasProfiles("eclipse");
    }

    protected boolean hasProfiles(String... profiles) {
        return environment.acceptsProfiles(profiles);
    }

    protected boolean noProfiles(String... profiles) {
        return !environment.acceptsProfiles(profiles);
    }

    protected boolean isLoaded(Object entity, String attribute) {
        return Persistence.getPersistenceUtil().isLoaded(entity, attribute);
    }

}
