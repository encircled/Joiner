package cz.encircled.joiner.core;

import cz.encircled.joiner.config.hint.HintQueryFeature;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kisel on 04.02.2016.
 */
public abstract class HintsTest extends AbstractTest {

    @Test
    public void testHint() {
        joiner.find(Q.from(QUser.user1).addHint("testHint", "testHintValue").addFeatures(new HintQueryFeature()));
    }

    @Test
    public void testDefaultHint() {
        try {
            joiner.setJoinerProperties(new JoinerProperties()
                    .addDefaultHint("testHint", "testHintValue"));
            joiner.find(Q.from(QUser.user1).addFeatures(new HintQueryFeature()));

            joiner.getJoinerProperties().removeDefaultHint("testHint");

            assertThrows(NoSuchElementException.class, () -> joiner.find(Q.from(QUser.user1).addFeatures(new HintQueryFeature())));
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

    @Test
    public void testOfTest() {
        assertThrows(TestException.class, () -> joiner.find(Q.from(QUser.user1).addHint("testHint", "exception").addFeatures(new HintQueryFeature())));
    }

    @Test
    public void testNullKeyHint() {
        assertThrows(IllegalArgumentException.class, () -> joiner.find(Q.from(QUser.user1).addHint(null, "testHintValue").addFeatures(new HintQueryFeature())));
    }

}
