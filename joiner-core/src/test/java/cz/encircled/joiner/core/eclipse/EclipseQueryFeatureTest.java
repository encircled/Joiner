package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.QueryFeatureTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseQueryFeatureTest extends QueryFeatureTest {
}
