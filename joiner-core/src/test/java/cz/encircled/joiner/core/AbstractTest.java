package cz.encircled.joiner.core;

import cz.encircled.joiner.TestDataListener;
import cz.encircled.joiner.TestWithLogging;
import cz.encircled.joiner.config.TestConfig;
import cz.encircled.joiner.model.AbstractEntity;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.join.JoinGraphRegistry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.spi.LoadState;
import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolverHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Kisel on 11.01.2016.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
@Transactional
@EnableTransactionManagement
@TestExecutionListeners(listeners = {TestDataListener.class, DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
public abstract class AbstractTest extends TestWithLogging {

    @Autowired
    protected Joiner joiner;

    @Autowired
    protected JoinGraphRegistry joinGraphRegistry;

    @PersistenceContext
    protected EntityManager entityManager;

    @Value("${orm:hibernate}")
    private String orm;

    protected void assertHasName(Collection<? extends AbstractEntity> entities, String name) {
        Assertions.assertFalse(entities.isEmpty(), "Found collection must be not empty!");
        for (AbstractEntity entity : entities) {
            assertHasName(entity, name);
        }
    }

    protected void assertHasName(AbstractEntity entity, String name) {
        Assertions.assertNotNull(entity);
        Assertions.assertEquals(name, entity.getName());
    }

    protected boolean isEclipse() {
        return orm.equals("eclipse");
    }

    protected boolean isHibernate() {
        return !isEclipse();
    }

    protected boolean isLoaded(Object entity, String attribute) {
        List<PersistenceProvider> providers = PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();
        Assertions.assertEquals(2, providers.size());
        PersistenceProvider eclipse = providers.stream().filter(p -> p.getClass().getName().contains("eclipse")).findFirst().orElseThrow();
        PersistenceProvider hibernate = providers.stream().filter(p -> !p.getClass().getName().contains("eclipse")).findFirst().orElseThrow();
        return doIsLoaded(entity, attribute, isEclipse() ? eclipse : hibernate);
    }

    private boolean doIsLoaded(Object entity, String attribute, PersistenceProvider provider) {
        LoadState state = provider.getProviderUtil().isLoadedWithoutReference(entity, attribute);
        if (state == LoadState.UNKNOWN) {
            state = provider.getProviderUtil().isLoadedWithReference(entity, attribute);
        }
        return state != LoadState.NOT_LOADED;
    }

    void assertQueryContains(String expected, JoinerQuery<?, ?> query) {
        String actual = joiner.toJPAQuery2(query).queryString.replaceAll(" join fetch ", " join ");
        assertTrue(actual.contains(expected), "actual: " + actual);
    }

}
