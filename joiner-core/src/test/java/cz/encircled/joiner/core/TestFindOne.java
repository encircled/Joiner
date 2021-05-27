package cz.encircled.joiner.core;

import cz.encircled.joiner.exception.JoinerException;
import cz.encircled.joiner.model.QUser;
import cz.encircled.joiner.query.Q;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Vlad on 11-Feb-17.
 */
public class TestFindOne extends AbstractTest {

    @Test
    public void testFindOneMultipleResults() {
        assertThrows(JoinerException.class, () -> joiner.findOne(Q.from(QUser.user1)));
    }

    @Test
    public void findOneReturnNull() {
        Assertions.assertNull(joiner.findOne(Q.from(QUser.user1).where(QUser.user1.id.isNull())));
    }

}
