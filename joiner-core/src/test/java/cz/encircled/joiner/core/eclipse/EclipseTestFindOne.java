package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.TestFindOne;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseTestFindOne extends TestFindOne {
}
