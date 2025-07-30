package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.FindStreamTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseFindStreamTest extends FindStreamTest {
}
