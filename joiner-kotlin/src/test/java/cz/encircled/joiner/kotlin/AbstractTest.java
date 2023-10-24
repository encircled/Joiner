package cz.encircled.joiner.kotlin;

import cz.encircled.joiner.TestDataListener;
import cz.encircled.joiner.TestWithLogging;
import cz.encircled.joiner.core.Joiner;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

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

}
