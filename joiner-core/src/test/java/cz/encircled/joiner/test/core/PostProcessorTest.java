package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.TestConfigWithFailPostProcessor;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class, TestConfigWithFailPostProcessor.class })
public class PostProcessorTest extends AbstractTest {

    @Test(expected = TestException.class)
    public void testPostProcessorIsCalled() {
        userRepository.find(new Q<User>());
    }

}
