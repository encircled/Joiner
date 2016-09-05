package cz.encircled.joiner.test.core;

import cz.encircled.joiner.query.Q;
import cz.encircled.joiner.test.config.TestConfig;
import cz.encircled.joiner.test.config.hint.HintQueryFeature;
import cz.encircled.joiner.test.model.QUser;
import cz.encircled.joiner.test.model.User;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Kisel on 04.02.2016.
 */
@ContextConfiguration(classes = {TestConfig.class})
public class HintsTest extends AbstractTest {

    @Test
    public void testHint() {
        joiner.find(Q.from(QUser.user1).addHint("testHint", "testHintValue").addFeatures(new HintQueryFeature()));
    }

    @Test(expected = TestException.class)
    public void testOfTest() {
        joiner.find(Q.from(QUser.user1).addHint("testHint", "exception").addFeatures(new HintQueryFeature()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKeyHint() {
        joiner.find(new Q<User>(from).addHint(null, "testHintValue").addFeatures(new HintQueryFeature()));
    }

}
