package cz.encircled.joiner.core.eclipse;

import cz.encircled.joiner.core.PaginationAndOrderTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "orm=eclipse")
public class EclipsePaginationAndOrderTest extends PaginationAndOrderTest {
}
