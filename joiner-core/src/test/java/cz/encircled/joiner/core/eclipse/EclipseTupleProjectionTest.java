package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.TupleProjectionTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipseTupleProjectionTest extends TupleProjectionTest {
}
