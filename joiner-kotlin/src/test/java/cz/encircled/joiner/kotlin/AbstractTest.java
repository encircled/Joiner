package cz.encircled.joiner.kotlin;

import cz.encircled.joiner.TestDataListener;
import cz.encircled.joiner.TestWithLogging;
import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.model.AbstractEntity;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.util.AssertionErrors.assertFalse;

/**
 * @author Kisel on 11.01.2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@TestExecutionListeners(listeners = {TestDataListener.class, DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
public abstract class AbstractTest extends TestWithLogging {

    @Autowired
    protected Joiner joiner;

    @Autowired
    protected JoinGraphRegistry joinGraphRegistry;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private Environment environment;

    protected void assertHasName(Collection<? extends AbstractEntity> entities, String name) {
        assertFalse("Found collection must be not empty!", entities.isEmpty());
        for (AbstractEntity entity : entities) {
            assertHasName(entity, name);
        }
    }

    protected void assertHasName(AbstractEntity entity, String name) {
        assertNotNull(entity);
        assertEquals(name, entity.getName());
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
