package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.MultipleMappingsForSameClassOnSingleEntityTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseMultipleMappingsForSameClassOnSingleEntityTest extends MultipleMappingsForSameClassOnSingleEntityTest {
}
