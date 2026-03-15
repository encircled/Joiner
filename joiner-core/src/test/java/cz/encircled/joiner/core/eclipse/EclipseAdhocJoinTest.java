package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.AdhocJoinTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseAdhocJoinTest extends AdhocJoinTest {
}
