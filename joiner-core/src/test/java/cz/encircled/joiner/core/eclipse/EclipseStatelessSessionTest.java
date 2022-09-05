package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.AbstractTest;
import cz.encircled.joiner.core.JoinerProperties;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseStatelessSessionTest extends AbstractTest {

    @Test
    public void notSupported() {
        try {
            Assertions.assertThrows(IllegalStateException.class, () -> {
                joiner.setJoinerProperties(new JoinerProperties().setUseStatelessSessions(true));
                joiner.find(Q.from(QUser.user1));
            });
        } finally {
            joiner.setJoinerProperties(null);
        }
    }

}
