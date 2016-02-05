package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.property.TestConfigWithPropertyPostProcessor;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 05.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class, TestConfigWithPropertyPostProcessor.class })
public class CustomPropertiesTest extends AbstractTest {

    @Test
    public void testCustomProperty() {
        userRepository.find(new Q<User>().addCustomProperty("testProperty", "testPropertyValue"));
    }

    @Test(expected = TestException.class)
    public void testOfTest() {
        userRepository.find(new Q<User>().addCustomProperty("testProperty", "exception"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyProperty() {
        userRepository.find(new Q<User>().addCustomProperty(null, "testPropertyValue"));
    }

}
