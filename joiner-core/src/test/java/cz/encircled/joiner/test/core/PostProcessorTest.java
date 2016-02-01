package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.TestConfigWithPostProcessor;
import cz.encircled.joiner.test.model.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 01.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class, TestConfigWithPostProcessor.class })
public class PostProcessorTest extends AbstractTest {

    @Test
    public void testPostProcessorIsCalled() {

        try {
            userRepository.find(new Q<User>());
            Assert.fail("PostProcessor has not been called");
        } catch (Exception e) {
            Assert.assertEquals("TestPostProcessor", e.getMessage());
        }

    }

}
