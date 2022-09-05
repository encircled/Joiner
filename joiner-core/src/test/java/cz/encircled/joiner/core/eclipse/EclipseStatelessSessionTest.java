package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.AbstractTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseStatelessSessionTest extends AbstractTest {

    @Test
    public void notSupported() {
        Assertions.assertThrows(IllegalStateException.class, () -> joiner.setUseStatelessSessions(true));
    }

}
