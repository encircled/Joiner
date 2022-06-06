package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.TestGroupBy;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseTestGroupBy extends TestGroupBy {
}
