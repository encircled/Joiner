package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.hint.TestConfigWithHintPostProcessor;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 04.02.2016.
 */
@ContextConfiguration(classes = { TestConfig.class, TestConfigWithHintPostProcessor.class })
public class HintsTest extends AbstractTest {

    @Test
    public void testHint() {
        userRepository.find(new Q<User>().addHint("testHint", "testHintValue"));
    }

    @Test(expected = TestException.class)
    public void testOfTest() {
        userRepository.find(new Q<User>().addHint("testHint", "exception"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyHint() {
        userRepository.find(new Q<User>().addHint(null, "testHintValue"));
    }

}
