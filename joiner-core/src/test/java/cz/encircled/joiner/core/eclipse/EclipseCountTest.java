package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.CountTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseCountTest extends CountTest {
}
