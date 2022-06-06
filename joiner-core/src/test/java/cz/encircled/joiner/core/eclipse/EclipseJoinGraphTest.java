package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.JoinGraphTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseJoinGraphTest extends JoinGraphTest {
}
