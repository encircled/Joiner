package cz.encircled.joiner;

import cz.encircled.joiner.config.SpringTestConfig;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(classes = {SpringTestConfig.class})
@Transactional
@EnableTransactionManagement
@TestExecutionListeners(listeners = {TestDataListener.class, DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
public abstract class AbstractSpringJoinerTest {
}
