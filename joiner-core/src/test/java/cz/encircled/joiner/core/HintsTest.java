package cz.encircled.joiner.core;

import cz.encircled.joiner.config.TestConfig;
import cz.encircled.joiner.config.hint.HintQueryFeature;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
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
        joiner.find(Q.from(QUser.user1).addHint(null, "testHintValue").addFeatures(new HintQueryFeature()));
    }

}
