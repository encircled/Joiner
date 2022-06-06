package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.FailTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseFailTest extends FailTest {
}
