package cz.encircled.joiner.core;

import cz.encircled.joiner.model.QStatus;
import cz.encircled.joiner.model.Status;
import cz.encircled.joiner.query.JoinerQuery;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class FindStreamTest extends AbstractTest {

    // Cursor fetch is not supported by H2, so this test is very synthetic for now.
    @Test
    public void testStreamAll() {
        JoinerQuery<Status, Status> q = Q.from(QStatus.status);
        assertEquals(
                joiner.find(q).size(),
                joiner.findStream(q).toList().size()
        );
    }

}
