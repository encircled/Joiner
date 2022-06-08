package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.ResultProjectionTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseResultProjectionTest extends ResultProjectionTest {
}
