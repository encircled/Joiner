package cz.encircled.joiner.eclipse;

import cz.encircled.joiner.TestDataListener;
import cz.encircled.joiner.TestWithLogging;
import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.model.AbstractEntity;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author Kisel on 11.01.2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@TestExecutionListeners(listeners = {TestDataListener.class, DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
@TestPropertySource(properties = "orm=eclipse")
public abstract class AbstractEclipseTest extends TestWithLogging {

    @Autowired
    protected Joiner joiner;

    @Autowired
    protected JoinGraphRegistry joinGraphRegistry;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private Environment environment;

    @BeforeEach
    public void before() {
        assumeTrue(isEclipse());

        entityManager.clear();
        entityManager.getEntityManagerFactory().getCache().evictAll();
    }

    protected void assertHasName(AbstractEntity entity, String name) {
        assertNotNull(entity);
        assertEquals(name, entity.getName());
    }

    protected boolean isEclipse() {
        return true;//environment.acceptsProfiles(Profiles.of("eclipse"));
    }

    protected boolean isLoaded(Object entity, String attribute) {
        return Persistence.getPersistenceUtil().isLoaded(entity, attribute);
    }

}
