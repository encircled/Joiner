package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.GroupByTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseGroupByTest extends GroupByTest {
}
